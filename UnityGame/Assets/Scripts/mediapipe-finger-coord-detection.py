import cv2
import mediapipe as mp
import numpy as np
import socket
import time


class FingerTouchDetector:
    def __init__(self):
        # Initialize MediaPipe hands module
        # Add UDP socket setup
        self.data_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.unity_address = ('127.0.0.1', 5000)
        self.mp_hands = mp.solutions.hands
        self.hands = self.mp_hands.Hands(
            static_image_mode=False,
            max_num_hands=1,
            min_detection_confidence=0.7,
            min_tracking_confidence=0.5
        )  # Remove the [1] that was causing the error

        # Define finger indices
        self.finger_tips = {
            1: 8,   # Index finger
            2: 12,  # Middle finger
            3: 16,  # Ring finger
            4: 20   # Pinky finger
        }
        self.thumb_tip = 4

        # Distance threshold for touch detection
        self.touch_threshold = 0.04

    def detect_touches(self, frame):
        # Convert BGR to RGB
        rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)

        # Process the frame
        results = self.hands.process(rgb_frame)

        touched_finger = 0

        if results.multi_hand_landmarks:
            hand_landmarks = results.multi_hand_landmarks[0]

            # Get thumb tip coordinates
            thumb_pos = np.array([
                hand_landmarks.landmark[self.thumb_tip].x,
                hand_landmarks.landmark[self.thumb_tip].y,
                hand_landmarks.landmark[self.thumb_tip].z
            ])

            # Check distance between thumb and each finger
            for finger_num, finger_tip_idx in self.finger_tips.items():
                finger_pos = np.array([
                    hand_landmarks.landmark[finger_tip_idx].x,
                    hand_landmarks.landmark[finger_tip_idx].y,
                    hand_landmarks.landmark[finger_tip_idx].z
                ])

                # Calculate Euclidean distance
                distance = np.linalg.norm(thumb_pos - finger_pos)

                if distance < self.touch_threshold:
                    touched_finger = finger_num
                    break
        try:
            data_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            unity_address = ('127.0.0.1', 5000)
            data_socket.sendto(str(touched_finger).encode(), unity_address)
            data_socket.close()  # Close the socket after sending
        except Exception as e:
            print(f"Error sending data to Unity: {e}")
        return touched_finger


def main():
    # Initialize camera
    cap = cv2.VideoCapture(0)  # This will use the built-in MacBook camera
    detector = FingerTouchDetector()

    while True:
        ret, frame = cap.read()
        if not ret:
            break

        # Mirror the frame horizontally
        frame = cv2.flip(frame, 1)

        # Detect which finger is being touched
        touched_finger = detector.detect_touches(frame)

        # Display the result
        text = f"Touching finger: {touched_finger if touched_finger > 0 else 'None'}"
        cv2.putText(frame, text, (10, 30),
                    cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2)

        cv2.imshow('Finger Touch Detection', frame)

        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

    cap.release()
    cv2.destroyAllWindows()


if __name__ == "__main__":
    main()
