import threading
import time
import math
import socket
import cv2
import mediapipe as mp
import mediapipe.python.solutions.drawing_utils as mp_drawing
import mediapipe.python.solutions.pose as mp_pose
import mediapipe.python.solutions.hands as mp_hands
import mediapipe.python.solutions.face_detection as mp_face_detection

# Toggle this in order to view how your WebCam is being interpreted (reduces performance).
DEBUG = False

# To switch cameras. Sometimes takes a while.
WEBCAM_INDEX = 0

# Settings do not universally apply, not all WebCams support all frame rates and resolutions
USE_CUSTOM_CAM_SETTINGS = False
FPS = 60
WIDTH = 320
HEIGHT = 240

# [0, 2] Higher numbers are more precise, but also cost more performance. Good environment conditions = 1, otherwise 2.
MODEL_COMPLEXITY = 0

# Server address for sending data
data_server_address = ("127.0.0.1", 5000)

class HandThread(threading.Thread):
    def __init__(self):
        super().__init__()
        self.data = ""
        self.dirty = False
        self.isRunning = False
        self.cap = None
        self.image = None
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)  # Create UDP socket
        print(f"Socket created: {self.sock}")  # Debug output

    def stop(self):
        self.isRunning = False
        if self.cap:
            self.cap.release()
        self.sock.close()  # Close the socket when stopping the thread
        print("Socket closed")  # Debug output

    def run(self):
        print("HandThread started")
        self.isRunning = True
        self.cap = cv2.VideoCapture(WEBCAM_INDEX)
        if USE_CUSTOM_CAM_SETTINGS:
            self.cap.set(cv2.CAP_PROP_FPS, FPS)
            self.cap.set(cv2.CAP_PROP_FRAME_WIDTH, WIDTH)
            self.cap.set(cv2.CAP_PROP_FRAME_HEIGHT, HEIGHT)
        print("HandThread: Camera opened")

        with mp_hands.Hands(
            min_detection_confidence=0.75,
            min_tracking_confidence=0.5,
            model_complexity=MODEL_COMPLEXITY,
        ) as hands, mp_pose.Pose(
            min_detection_confidence=0.80,
            min_tracking_confidence=0.5,
            model_complexity=MODEL_COMPLEXITY,
            static_image_mode=False,
            enable_segmentation=False,
        ) as pose, mp_face_detection.FaceDetection(min_detection_confidence=0.75) as face_detection:

            while self.isRunning:
                ret, frame = self.cap.read()
                if not ret:
                    print("HandThread: failed to capture frame")
                    self.isRunning = False
                    break
                image = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
                image = cv2.flip(image, 1)
                image.flags.writeable = DEBUG

                # Run detection
                hand_results = hands.process(image)
                pose_results = pose.process(image)
                face_results = face_detection.process(image)

                # Convert back to BGR for OpenCV rendering
                image = cv2.cvtColor(image, cv2.COLOR_RGB2BGR)

                # Process hand and pose landmarks
                self.data = ""
                hand_landmarks = hand_results.multi_hand_landmarks
                pose_landmarks = pose_results.pose_landmarks
                nose_position = None

                # Store pose (nose) data
                if pose_landmarks:
                    nose_position = (
                        pose_landmarks.landmark[mp_pose.PoseLandmark.NOSE].x,
                        pose_landmarks.landmark[mp_pose.PoseLandmark.NOSE].y,
                    )

                # Check if a face is detected
                if face_results.detections:
                    print("Face detected in frame.")
                    self.send_data("Face detected")

                # Check for hand and nose touch
                if hand_landmarks and nose_position:
                    for hand in hand_landmarks:
                        for i, landmark in enumerate(hand.landmark):
                            if i == 8:  # Tip of index finger
                                hand_pos = (landmark.x, landmark.y)
                                # Calculate Euclidean distance between index finger and nose
                                dist = math.sqrt(
                                    (hand_pos[0] - nose_position[0]) ** 2
                                    + (hand_pos[1] - nose_position[1]) ** 2
                                )
                                # Check if hand is touching nose (threshold can be adjusted)
                                if dist < 0.05:  # Distance threshold
                                    print("Hand is touching the nose!")
                                    self.send_data("Hand touching nose")
                                    cv2.putText(
                                        image,
                                        "Hand Touching Nose!",
                                        (50, 50),
                                        cv2.FONT_HERSHEY_SIMPLEX,
                                        1,
                                        (0, 255, 0),
                                        2,
                                        cv2.LINE_AA,
                                    )

                # Draw landmarks
                if hand_landmarks:
                    for hand in hand_landmarks:
                        mp_drawing.draw_landmarks(
                            image, hand, mp_hands.HAND_CONNECTIONS
                        )
                if pose_landmarks:
                    mp_drawing.draw_landmarks(
                        image,
                        pose_landmarks,
                        mp_pose.POSE_CONNECTIONS,
                        mp_drawing.DrawingSpec(
                            color=(255, 100, 0), thickness=5, circle_radius=8
                        ),
                        mp_drawing.DrawingSpec(
                            color=(255, 255, 255), thickness=5, circle_radius=4
                        ),
                    )

                self.image = image
                self.dirty = True

        print("HandThread stopped")

    def send_data(self, message):
        try:
            self.sock.sendto(message.encode(), data_server_address)
            print(f"Sent data: {message} to {data_server_address}")  # Debug output
        except Exception as e:
            print(f"Error sending data: {e}")

if __name__ == "__main__":
    hand_thread = HandThread()

    try:
        hand_thread.start()
        while hand_thread.is_alive():
            if hand_thread.dirty:
                image = hand_thread.image
                if DEBUG:
                    cv2.imshow("Hand and Body Tracking", image)
                    if cv2.waitKey(5) & 0xFF == ord("q"):
                        break
            time.sleep(0.016)
    except KeyboardInterrupt:
        print("Interrupt received, stopping...")
        hand_thread.stop()
        hand_thread.join()
        cv2.destroyAllWindows()
        print("Threads successfully stopped.")
