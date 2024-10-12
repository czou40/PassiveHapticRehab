import cv2
import mediapipe as mp

# Initialize MediaPipe hands and face mesh
mp_hands = mp.solutions.hands
mp_face_mesh = mp.solutions.face_mesh
hands = mp_hands.Hands(min_detection_confidence=0.5, min_tracking_confidence=0.5)
face_mesh = mp_face_mesh.FaceMesh(min_detection_confidence=0.5, min_tracking_confidence=0.5)

# Function to calculate distance between index finger and nose
def calculate_distance(hand_landmarks, face_landmarks):
    # Get nose tip landmark
    nose_tip = face_landmarks.landmark[1]
    # Get index finger tip landmark
    index_finger_tip = hand_landmarks.landmark[mp_hands.HandLandmark.INDEX_FINGER_TIP]
    # Calculate distance between nose tip and index finger tip
    distance = ((nose_tip.x - index_finger_tip.x) ** 2 + (nose_tip.y - index_finger_tip.y) ** 2) ** 0.5
    return distance

# Function to check if hand is touching nose
def is_touching_nose(hand_landmarks, face_landmarks, threshold_distance=0.05):
    distance = calculate_distance(hand_landmarks, face_landmarks)
    if distance < threshold_distance:
        return True
    return False

# Capture video from webcam
cap = cv2.VideoCapture(0)

while cap.isOpened():
    success, image = cap.read()
    if not success:
        print("Ignoring empty camera frame.")
        continue

    # Convert the BGR image to RGB.
    image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
    image.flags.writeable = False
    results_hands = hands.process(image)
    results_face_mesh = face_mesh.process(image)

    # Draw the hand and face mesh annotations on the image.
    image.flags.writeable = True
    image = cv2.cvtColor(image, cv2.COLOR_RGB2BGR)
    if results_hands.multi_hand_landmarks and results_face_mesh.multi_face_landmarks:
        for hand_landmarks, face_landmarks in zip(results_hands.multi_hand_landmarks, results_face_mesh.multi_face_landmarks):
            # Calculate distance between index finger and nose
            distance = calculate_distance(hand_landmarks, face_landmarks)
            print(f"Distance between index finger and nose: {distance}")

            # Check if hand is touching nose
            touching_nose = is_touching_nose(hand_landmarks, face_landmarks)
            print(f"Touching nose: {touching_nose}")

            # Draw hand landmarks
            mp_drawing = mp.solutions.drawing_utils
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

    # Flip the image horizontally for a later selfie-view display, and convert the color space back to BGR.
    cv2.imshow('MediaPipe Hand and Face Mesh', image)
    if cv2.waitKey(5) & 0xFF == 27:
        break
hands.close()
face_mesh.close()
cap.release()