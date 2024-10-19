import threading
import time
import math
import socket
import cv2
import mediapipe as mp
import mediapipe.python.solutions.drawing_utils as mp_drawing
import mediapipe.python.solutions.pose as mp_pose
import mediapipe.python.solutions.hands as mp_hands

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
    def __init__(self, data_socket, data_address):
        super().__init__()
        self.data_socket = data_socket
        self.data_address = data_address
        self.isRunning = False
        self.cap = None
        self.nose_touching_data = ""

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
        ) as hands, mp_pose.Pose(
            min_detection_confidence=0.80,
            min_tracking_confidence=0.5,
            model_complexity=MODEL_COMPLEXITY,
        ) as pose:
            while self.isRunning:
                ret, frame = self.cap.read()
                if not ret:
                    print("HandThread: failed to capture frame")
                    self.isRunning = False
                    break

                image = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
                image = cv2.flip(image, 1)

                # Run detection
                hand_results = hands.process(image)
                pose_results = pose.process(image)

                # Process hand and pose landmarks
                self.nose_touching_data = ""
                hand_landmarks = hand_results.multi_hand_landmarks
                pose_landmarks = pose_results.pose_landmarks
                nose_position = None

                # Store pose (nose) data
                if pose_landmarks:
                    nose_position = (
                        pose_landmarks.landmark[mp_pose.PoseLandmark.NOSE].x,
                        pose_landmarks.landmark[mp_pose.PoseLandmark.NOSE].y,
                    )

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
                                    self.nose_touching_data = "Hand touching the nose!"
                                    print(self.nose_touching_data)  # Print to terminal
                                    # Send data over UDP
                                    self.data_socket.sendto(self.nose_touching_data.encode("utf-8"), self.data_address)

                # Draw landmarks
                if hand_landmarks:
                    for hand in hand_landmarks:
                        mp_drawing.draw_landmarks(image, hand, mp_hands.HAND_CONNECTIONS)
                if pose_landmarks:
                    mp_drawing.draw_landmarks(image, pose_landmarks, mp_pose.POSE_CONNECTIONS)

                cv2.imshow("Hand and Body Tracking", image)
                if DEBUG and cv2.waitKey(5) & 0xFF == ord("q"):
                    break

        print("HandThread stopped")


if __name__ == "__main__":
    # Create a UDP socket
    data_client_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    data_server_address = ("127.0.0.1", 7777)

    hand_thread = HandThread(data_client_socket, data_server_address)

    try:
        hand_thread.start()
        while hand_thread.is_alive():
            time.sleep(0.016)
    except KeyboardInterrupt:
        print("Interrupt received, stopping...")
        hand_thread.stop()
        hand_thread.join()
        cv2.destroyAllWindows()
        data_client_socket.close()
        print("Threads successfully stopped.")
