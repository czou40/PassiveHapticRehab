import threading
import time
import math
import socket
import cv2
import mediapipe as mp
import os
import platform
import urllib.request
import numpy as np

from mediapipe.tasks import python
from mediapipe.tasks.python import vision

# Toggle this in order to view how your WebCam is being interpreted (reduces performance).
DEBUG = False

# Determine the operating system
current_os = platform.system()

# Set WEBCAM_INDEX based on the operating system
if current_os == "Darwin":  # macOS
    WEBCAM_INDEX = 0
else:  # Windows or Linux
    WEBCAM_INDEX = 0

# Settings do not universally apply, not all WebCams support all frame rates and resolutions
USE_CUSTOM_CAM_SETTINGS = False
FPS = 60
WIDTH = 320
HEIGHT = 240

HAND_LANDMARKER_MODEL_PATH = 'hand_landmarker.task'
POSE_LANDMARKER_MODEL_PATH = 'pose_landmarker_lite.task'

# Define Connections manually since we are avoiding mediapipe.solutions
HAND_CONNECTIONS = [
    (0, 1), (1, 2), (2, 3), (3, 4),
    (0, 5), (5, 6), (6, 7), (7, 8),
    (5, 9), (9, 10), (10, 11), (11, 12),
    (9, 13), (13, 14), (14, 15), (15, 16),
    (13, 17), (17, 18), (18, 19), (19, 20),
    (0, 17)
]

POSE_CONNECTIONS = [
    (0, 1), (1, 2), (2, 3), (3, 7),
    (0, 4), (4, 5), (5, 6), (6, 8),
    (9, 10),
    (11, 12), (11, 13), (13, 15), (15, 17), (15, 19), (15, 21), (17, 19),
    (12, 14), (14, 16), (16, 18), (16, 20), (16, 22), (18, 20),
    (11, 23), (12, 24), (23, 24),
    (23, 25), (24, 26), (25, 27), (26, 28), (27, 29), (28, 30),
    (29, 31), (30, 32), (27, 31), (28, 32)
]

def download_file(url, filename):
    if not os.path.exists(filename):
        print(f"Downloading {filename}...")
        try:
            urllib.request.urlretrieve(url, filename)
            print(f"Downloaded {filename}")
        except Exception as e:
            print(f"Failed to download {filename}: {e}")

def draw_landmarks_on_image(rgb_image, detection_result, connections, is_pose=False):
    """Custom function to draw landmarks and connections."""
    image = np.copy(rgb_image)
    height, width, _ = image.shape

    if is_pose:
        # Handle PoseLandmarkerResult
        if not detection_result.pose_landmarks:
            return image
        landmarks_list = detection_result.pose_landmarks
    else:
        # Handle HandLandmarkerResult
        if not detection_result.hand_landmarks:
            return image
        landmarks_list = detection_result.hand_landmarks

    for landmarks in landmarks_list:
        # Draw connections
        if connections:
            for connection in connections:
                start_idx = connection[0]
                end_idx = connection[1]
                
                if start_idx >= len(landmarks) or end_idx >= len(landmarks):
                    continue
                    
                start_point = landmarks[start_idx]
                end_point = landmarks[end_idx]
                
                start_x = int(start_point.x * width)
                start_y = int(start_point.y * height)
                end_x = int(end_point.x * width)
                end_y = int(end_point.y * height)
                
                cv2.line(image, (start_x, start_y), (end_x, end_y), (255, 255, 255), 2)

        # Draw landmarks
        for landmark in landmarks:
            x = int(landmark.x * width)
            y = int(landmark.y * height)
            
            color = (255, 100, 0) if is_pose else (0, 255, 0)
            radius = 4 if is_pose else 3
            
            cv2.circle(image, (x, y), radius, color, -1)
            
    return image

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

        # Download models
        download_file(
            "https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/1/hand_landmarker.task",
            HAND_LANDMARKER_MODEL_PATH
        )
        download_file(
            "https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_lite/float16/1/pose_landmarker_lite.task",
            POSE_LANDMARKER_MODEL_PATH
        )

        # Initialize Hand Landmarker
        base_options_hand = python.BaseOptions(model_asset_path=HAND_LANDMARKER_MODEL_PATH)
        options_hand = vision.HandLandmarkerOptions(
            base_options=base_options_hand,
            running_mode=vision.RunningMode.VIDEO,
            num_hands=2,
            min_hand_detection_confidence=0.75,
            min_tracking_confidence=0.5,
            min_hand_presence_confidence=0.5
        )
        
        # Initialize Pose Landmarker
        base_options_pose = python.BaseOptions(model_asset_path=POSE_LANDMARKER_MODEL_PATH)
        options_pose = vision.PoseLandmarkerOptions(
            base_options=base_options_pose,
            running_mode=vision.RunningMode.VIDEO,
            num_poses=1,
            min_pose_detection_confidence=0.8,
            min_tracking_confidence=0.5,
            min_pose_presence_confidence=0.5
        )

        self.isRunning = True
        self.cap = cv2.VideoCapture(WEBCAM_INDEX)

        if not self.cap.isOpened():
            print(f"HandThread: Failed to open camera at index {WEBCAM_INDEX}. Trying index 0.")
            self.cap = cv2.VideoCapture(0)

        if not self.cap.isOpened():
            print("HandThread: Failed to open camera.")
            self.isRunning = False
            return

        if USE_CUSTOM_CAM_SETTINGS:
            self.cap.set(cv2.CAP_PROP_FPS, FPS)
            self.cap.set(cv2.CAP_PROP_FRAME_WIDTH, WIDTH)
            self.cap.set(cv2.CAP_PROP_FRAME_HEIGHT, HEIGHT)
        print("HandThread: Camera opened")

        try:
            with vision.HandLandmarker.create_from_options(options_hand) as hand_detector, \
                 vision.PoseLandmarker.create_from_options(options_pose) as pose_detector:
                
                start_time = time.time()
                
                while self.isRunning:
                    ret, frame = self.cap.read()
                    if not ret:
                        print("HandThread: failed to capture frame")
                        self.isRunning = False
                        break
                    
                    # Preprocessing
                    # MediaPipe Tasks expects RGB images
                    image_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
                    image_rgb = cv2.flip(image_rgb, 1)
                    
                    # Create MP Image
                    mp_image = mp.Image(image_format=mp.ImageFormat.SRGB, data=image_rgb)
                    
                    # Calculate timestamp in ms
                    current_time = time.time()
                    frame_timestamp_ms = int((current_time - start_time) * 1000)
                    if frame_timestamp_ms == 0: frame_timestamp_ms = 1 # Avoid 0

                    # Detect Hands
                    hand_results = hand_detector.detect_for_video(mp_image, frame_timestamp_ms)
                    
                    # Detect Pose
                    pose_results = pose_detector.detect_for_video(mp_image, frame_timestamp_ms)
                    
                    self.data = ""
                    
                    # Process Hand Results
                    if hand_results.hand_world_landmarks:
                        print(f"HandThread: Detected {len(hand_results.hand_world_landmarks)} hand(s).")
                        for j in range(len(hand_results.handedness)):
                            hand_world_landmarks = hand_results.hand_world_landmarks[j]
                            handedness_category = hand_results.handedness[j][0]
                            
                            for i in range(0, 21):
                                self.data += "{}|{}|{}|{}|{}\n".format(
                                    handedness_category.category_name, # "Left" or "Right"
                                    i,
                                    hand_world_landmarks[i].x,
                                    hand_world_landmarks[i].y,
                                    hand_world_landmarks[i].z,
                                )

                    # Process Pose Results
                    if pose_results.pose_world_landmarks:
                        # pose_world_landmarks is a list of lists (one per detected pose)
                        for pose_landmarks in pose_results.pose_world_landmarks:
                            for i in range(0, 33):
                                self.data += "Pose|{}|{}|{}|{}\n".format(
                                    i, 
                                    pose_landmarks[i].x, 
                                    pose_landmarks[i].y, 
                                    pose_landmarks[i].z
                                )
                            
                            # Add visibility
                            self.data += 'VisibilityPose|'
                            self.data += '|'.join([str(pose_landmarks[i].visibility) for i in range(0, 33)])
                            self.data += '\n'
                            # We only process the first pose if multiple are detected, based on original code structure
                            break 

                    self.dirty = True
                    
                    # Draw Landmarks for visualization
                    annotated_image = image_rgb.copy()
                    
                    if hand_results.hand_landmarks:
                        annotated_image = draw_landmarks_on_image(annotated_image, hand_results, HAND_CONNECTIONS, is_pose=False)
                        
                    if pose_results.pose_landmarks:
                        annotated_image = draw_landmarks_on_image(annotated_image, pose_results, POSE_CONNECTIONS, is_pose=True)
                    
                    # Convert back to BGR for display/UDP
                    self.image = cv2.cvtColor(annotated_image, cv2.COLOR_RGB2BGR)

        except Exception as e:
            print(f"HandThread Error: {e}")
            self.stop()
            
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
                if data_unencoded:
                    data = data_unencoded.encode("utf-8")
                    data_client_socket.sendto(data, data_server_address)
                hand_thread.dirty = False
                image = hand_thread.image
                if image is not None:
                    height, width = image.shape[:2]
                    aspect_ratio = width / height
                    new_width = int(math.sqrt(100000 * aspect_ratio))
                    new_height = int(100000 / new_width)
                    resized_image = cv2.resize(image, (new_width, new_height), interpolation=cv2.INTER_LINEAR)
                    
                    # Encode image as JPEG with lower quality
                    _, buffer = cv2.imencode('.jpg', resized_image, [int(cv2.IMWRITE_JPEG_QUALITY), 10])  # Adjust quality here
                    image_data = buffer.tobytes()
                    # clear screen
                    os.system('cls' if os.name == 'nt' else 'clear')
                    print(f"Size of image data: {len(image_data)/1024} KB")
                    # print(data_unencoded) # Optional: print data
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
