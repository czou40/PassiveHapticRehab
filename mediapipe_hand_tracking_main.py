import threading
import time
import math
import socket
from queue import Queue 
import cv2
import mediapipe as mp
import mediapipe.python.solutions.drawing_utils as mp_drawing
import mediapipe.python.solutions.pose as mp_pose
import mediapipe.python.solutions.hands as mp_hands
import os

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


class HandThread(threading.Thread):
    def __init__(self):
        super().__init__()
        self.data = ""
        self.poseData = ""
        self.dirty = False
        self.isRunning = False
        self.cap = None
        self.image = None

    def stop(self):
        self.isRunning = False
        if self.cap:
            self.cap.release()

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
        ) as hands:
            with mp_pose.Pose(
                min_detection_confidence=0.80,
                min_tracking_confidence=0.5,
                model_complexity=MODEL_COMPLEXITY,
                static_image_mode=False,
                enable_segmentation=False,
            ) as pose:
                while self.isRunning:
                    ret, frame = self.cap.read()
                    if not ret:
                        print("HandThread: failed to capture frame")
                        self.isRunning = False
                        break
                    image = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
                    image = cv2.flip(image, 1)
                    image.flags.writeable = DEBUG
                    hand_results = hands.process(image)
                    pose_results = pose.process(image)
                    image = cv2.cvtColor(image, cv2.COLOR_RGB2BGR)

                    self.data = ""
                    if hand_results and hand_results.multi_hand_world_landmarks:
                        print(
                            f"HandThread: Deteceted {len(hand_results.multi_hand_world_landmarks)} hand(s)."
                        )
                        for j in range(len(hand_results.multi_handedness)):
                            hand_world_landmarks = (
                                hand_results.multi_hand_world_landmarks[j]
                            )
                            for i in range(0, 21):
                                self.data += "{}|{}|{}|{}|{}\n".format(
                                    hand_results.multi_handedness[j]
                                    .classification[0]
                                    .label,
                                    i,
                                    hand_world_landmarks.landmark[i].x,
                                    hand_world_landmarks.landmark[i].y,
                                    hand_world_landmarks.landmark[i].z,
                                )
                            
                            # visibility for hand landmarks is always 0.0, so commented out
                            # self.data += 'Visibility{}|'.format(hand_results.multi_handedness[j].classification[0].label)
                            # self.data += '|'.join([str(v) for v in [hand_world_landmarks.landmark[i].visibility for i in range(0, 21)]])
                            # self.data += '\n'

                    if pose_results.pose_world_landmarks:
                        for i in range(0,33):
                            self.data += "Pose|{}|{}|{}|{}\n".format(i, pose_results.pose_world_landmarks.landmark[i].x, pose_results.pose_world_landmarks.landmark[i].y, pose_results.pose_world_landmarks.landmark[i].z)
                        self.data += 'VisibilityPose|'
                        self.data += '|'.join([str(v) for v in [pose_results.pose_world_landmarks.landmark[i].visibility for i in range(0, 33)]])
                        self.data += '\n'


                    self.dirty = True
                    if hand_results.multi_hand_landmarks:
                        for hand in hand_results.multi_hand_landmarks:
                            mp_drawing.draw_landmarks(
                                image, hand, mp_hands.HAND_CONNECTIONS
                            )
                    if pose_results.pose_landmarks:
                        mp_drawing.draw_landmarks(
                            image,
                            pose_results.pose_landmarks,
                            mp_pose.POSE_CONNECTIONS,
                            mp_drawing.DrawingSpec(
                                color=(255, 100, 0), thickness=5, circle_radius=8
                            ),
                            mp_drawing.DrawingSpec(
                                color=(255, 255, 255), thickness=5, circle_radius=4
                            ),
                        )
                    self.image = image
        print("HandThread stopped")


if __name__ == "__main__":
    hand_thread = HandThread()

    # Create a UDP socket
    data_client_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    data_server_address = ("127.0.0.1", 7777)
    
    image_client_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    image_server_address = ("127.0.0.1", 7778)

    try:
        hand_thread.start()
        while hand_thread.is_alive():
            if hand_thread.dirty:
                data_unencoded = hand_thread.data
                data = data_unencoded.encode("utf-8")
                data_client_socket.sendto(data, data_server_address)
                hand_thread.dirty = False
                image = hand_thread.image
                height, width = image.shape[:2]
                aspect_ratio = width / height
                new_width = int(math.sqrt(100000 * aspect_ratio))
                new_height = int(100000 / new_width)
                resized_image = cv2.resize(image, (new_width, new_height), interpolation=cv2.INTER_LINEAR)
                image = resized_image
                # Encode image as JPEG with lower quality
                _, buffer = cv2.imencode('.jpg', image, [int(cv2.IMWRITE_JPEG_QUALITY), 10])  # Adjust quality here
                image_data = buffer.tobytes()
                # clear screen
                os.system('cls' if os.name == 'nt' else 'clear')
                print(f"Size of image data: {len(image_data)/1024} KB")
                print(data_unencoded)
                try:
                    image_client_socket.sendto(image_data, image_server_address)
                except Exception as e:
                    print(f"Failed to send image data: {e}")
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
        data_client_socket.close()
        image_client_socket.close()
        print("Threads successfully stopped.")