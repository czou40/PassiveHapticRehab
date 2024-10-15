import threading
import time
import math
import socket
import cv2
import mediapipe as mp
import os

# Toggle this in order to view how your WebCam is being interpreted (reduces performance).
DEBUG = False

# To switch cameras. Sometimes takes a while.
WEBCAM_INDEX = 1

# Settings do not universally apply, not all WebCams support all frame rates and resolutions
USE_CUSTOM_CAM_SETTINGS = False
FPS = 60
WIDTH = 320
HEIGHT = 240

# Threshold distance to determine if the hand is touching the nose
THRESHOLD_DISTANCE = 0.05

class NoseTouchThread(threading.Thread):
    def __init__(self):
        super().__init__()
        self.data = ""
        self.dirty = False
        self.isRunning = False
        self.cap = None
        self.image = None

    def stop(self):
        self.isRunning = False
        if self.cap:
            self.cap.release()

    def calculate_distance(self, hand_landmarks, face_landmarks):
        # Get nose tip landmark
        nose_tip = face_landmarks.landmark[1]
        # Get index finger tip landmark
        index_finger_tip = hand_landmarks.landmark[mp.solutions.hands.HandLandmark.INDEX_FINGER_TIP]
        # Calculate distance between nose tip and index finger tip
        distance = ((nose_tip.x - index_finger_tip.x) ** 2 + (nose_tip.y - index_finger_tip.y) ** 2) ** 0.5
        return distance

    def is_touching_nose(self, hand_landmarks, face_landmarks):
        distance = self.calculate_distance(hand_landmarks, face_landmarks)
        return distance < THRESHOLD_DISTANCE

    def run(self):
        print("NoseTouchThread started")
        self.isRunning = True
        self.cap = cv2.VideoCapture(WEBCAM_INDEX)
        if USE_CUSTOM_CAM_SETTINGS:
            self.cap.set(cv2.CAP_PROP_FPS, FPS)
            self.cap.set(cv2.CAP_PROP_FRAME_WIDTH, WIDTH)
            self.cap.set(cv2.CAP_PROP_FRAME_HEIGHT, HEIGHT)
        print("NoseTouchThread: Camera opened")

        mp_hands = mp.solutions.hands
        mp_face_mesh = mp.solutions.face_mesh

        with mp_hands.Hands(min_detection_confidence=0.5, min_tracking_confidence=0.5) as hands, \
             mp_face_mesh.FaceMesh(min_detection_confidence=0.5, min_tracking_confidence=0.5) as face_mesh:
            
            mp_drawing = mp.solutions.drawing_utils
            
            while self.isRunning:
                success, frame = self.cap.read()

                if not success:
                    print("NoseTouchThread: Ignoring empty camera frame.")
                    continue

                # Convert the BGR image to RGB
                image = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
                image.flags.writeable = False
                results_hands = hands.process(image)
                results_face_mesh = face_mesh.process(image)

                image.flags.writeable = True
                image = cv2.cvtColor(image, cv2.COLOR_RGB2BGR)

                if results_hands.multi_hand_landmarks and results_face_mesh.multi_face_landmarks:
                    for hand_landmarks, face_landmarks in zip(results_hands.multi_hand_landmarks, results_face_mesh.multi_face_landmarks):
                        # Check if hand is touching nose
                        touching_nose = self.is_touching_nose(hand_landmarks, face_landmarks)
                        print(f"Touching nose: {touching_nose}")

                        # Draw hand landmarks
                        mp_drawing.draw_landmarks(
                            image,
                            hand_landmarks,
                            mp_hands.HAND_CONNECTIONS,
                            mp_drawing.DrawingSpec(color=(121, 22, 76), thickness=2, circle_radius=4),
                            mp_drawing.DrawingSpec(color=(250, 44, 250), thickness=2, circle_radius=2),
                        )
                        # Draw face mesh landmarks
                        mp_drawing.draw_landmarks(
                            image,
                            face_landmarks,
                            mp_face_mesh.FACEMESH_CONTOURS,
                            mp_drawing.DrawingSpec(color=(80, 0, 140), thickness=1, circle_radius=1),
                            mp_drawing.DrawingSpec(color=(0, 0, 255), thickness=1, circle_radius=1),
                        )


                # Flip the image horizontally for a later selfie-view display
                self.image = image
                if DEBUG:
                    cv2.imshow('MediaPipe Nose Touch Detection', image)
                    if cv2.waitKey(5) & 0xFF == ord('q'):
                        break
        print("NoseTouchThread stopped")

if __name__ == "__main__":
    nose_touch_thread = NoseTouchThread()

    # Create a UDP socket
    data_client_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    data_server_address = ("127.0.0.1", 5000)

    image_client_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    image_server_address = ("127.0.0.1", 5001)

    try:
        nose_touch_thread.start()
        while nose_touch_thread.is_alive():
            if nose_touch_thread.dirty:
                data_unencoded = nose_touch_thread.data
                data = data_unencoded.encode("utf-8")
                data_client_socket.sendto(data, data_server_address)
                nose_touch_thread.dirty = False
                image = nose_touch_thread.image

                height, width = image.shape[:2]
                aspect_ratio = width / height
                new_width = int(math.sqrt(100000 * aspect_ratio))
                new_height = int(100000 / new_width)
                resized_image = cv2.resize(image, (new_width, new_height), interpolation=cv2.INTER_LINEAR)
                image = resized_image
                # Encode image as JPEG with lower quality
                _, buffer = cv2.imencode('.jpg', image, [int(cv2.IMWRITE_JPEG_QUALITY), 10])  # Adjust quality here
                image_data = buffer.tobytes()

                # Clear screen
                os.system('cls' if os.name == 'nt' else 'clear')
                print(f"Size of image data: {len(image_data)/1024} KB")
                print(self.data)

                try:
                    # Send image data here (you need to define image_client_socket and image_server_address)
                    image_client_socket.sendto(image_data, image_server_address)
                except Exception as e:
                    print(f"Failed to send image data: {e}")


                # You can also process and send the image if needed
                if DEBUG:
                    cv2.imshow('Nose Touch Detection', image)
                    if cv2.waitKey(5) & 0xFF == ord('q'):
                        break
            time.sleep(0.016)
    except KeyboardInterrupt:
        print("Interrupt received, stopping...")
        nose_touch_thread.stop()
        nose_touch_thread.join()
        cv2.destroyAllWindows()
        data_client_socket.close()
        print("Threads successfully stopped.")
