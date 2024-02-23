import mediapipe as mp
import cv2
import threading
import time
import socket


DEBUG = False
MODEL_COMPLEXITY = 0
        

class HandThread(threading.Thread):
    def __init__(self):
        super().__init__()
        self.data = ""
        self.dirty = False
        self.isRunning = False
        self.cap = None
        self.ret = None
        self.frame = None


    def stop(self):
        self.isRunning = False
        if self.cap:
            self.cap.release()

    def run(self):
        print("HandThread started")
        self.isRunning = True
        self.cap = cv2.VideoCapture(0)
        print("HandThread: Camera opened")
        mp_drawing = mp.solutions.drawing_utils
        mp_hands = mp.solutions.hands


        with mp_hands.Hands(min_detection_confidence=0.75, min_tracking_confidence=0.5, model_complexity=MODEL_COMPLEXITY) as hands:
            while self.isRunning:
                self.ret, self.frame = self.cap.read()
                if not self.ret:
                    print('HandThread: failed to capture frame')
                    self.isRunning = False
                image = cv2.cvtColor(self.frame, cv2.COLOR_BGR2RGB)
                image = cv2.flip(image, 1)
                image.flags.writeable = DEBUG
                results = hands.process(image)
                image = cv2.cvtColor(image, cv2.COLOR_RGB2BGR)

                self.data = ""
                if results.multi_hand_world_landmarks:
                    print(f"HandThread: Deteceted {len(results.multi_hand_world_landmarks)} hand(s).")
                    for j in range(len(results.multi_handedness)):
                        hand_world_landmarks = results.multi_hand_world_landmarks[j]
                        for i in range(0, 21):
                            self.data += "{}|{}|{}|{}|{}\n".format(
                                results.multi_handedness[j].classification[0].label,
                                i,
                                hand_world_landmarks.landmark[i].x,
                                hand_world_landmarks.landmark[i].y,
                                hand_world_landmarks.landmark[i].z)
                self.dirty = True

                if DEBUG and results.multi_hand_landmarks:
                    for hand in results.multi_hand_landmarks:
                        mp_drawing.draw_landmarks(image, hand, mp_hands.HAND_CONNECTIONS)
                    cv2.imshow('Hand Tracking', image)
                    if cv2.waitKey(5) & 0xFF == ord('q'):
                        break
        cv2.destroyAllWindows()
        print("HandThread stopped")

if __name__ == '__main__':
    hand_thread = HandThread()
    
    # Create a UDP socket
    client_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    server_address = ('127.0.0.1', 7777)

    try:
        hand_thread.start()
        while hand_thread.is_alive():
            if hand_thread.dirty:
                data = hand_thread.data.encode('utf-8')
                # TODO: send data to server 127.0.0.1:7777 using udp
                client_socket.sendto(data, server_address)
                hand_thread.dirty = False
            time.sleep(0.016)
    except KeyboardInterrupt:
        print("Interrupt received, stopping...")
        hand_thread.stop()
        hand_thread.join()
        print("Threads successfully stopped.")
