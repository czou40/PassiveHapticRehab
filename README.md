This project contains all the code for the passive haptic rehab (PHR) project. It contains a native Android app and a Unity project. A poster for this project can be found [here](https://docs.google.com/presentation/d/10tu4jEJqZrs_JyMw_0E2hgDyATYgn88k/edit?usp=sharing&ouid=101302997746283550194&rtpof=true&sd=true).

# Unity Project

The Unity project is independent of the Android app and can run on its own (However, if you export the project and launch it, it will just display the default scene, which is the loading screen. To specify which game to run, the Android app is required. In the Unity editor, you can directly choose any game scene to run.) The Unity games require data input from the MediaPipe hand and pose tracking models. We provide a convenient Python script to run the MediaPipe hand tracking model and send the data to the Unity project using UDP sockets. When running on Android, the app will handle the data input and send it to the Unity project.

## Instructions
1. Requires Python, Unity (6000.0.1f1, released in 2024. When installing Unity, please make sure Android Build Support is also selected), a WebCam, and decently fast CPU.
2. pip install mediapipe==0.10.9 (we recommend creating a virtual environment and installing mediapipe in it, using either the native venv, conda, or poetry.)
3. Run the Unity project and mediapipe_hand_tracking_main.py. If you see a warning by the Unity Editor saying there is a deprecated dependency, you can ignore it (the deprecated dependency is Visual Studio Code Editor and is not necessary for the project to run).

## Tips
* You can set the DEBUG flag True in hands.py to visualize what is being seen and how your hands are being interpreted.
* Improve the accuracy of the model by setting MODEL_COMPLEXITY to 1 inside hands.py
* Make sure port 7777 and 7778 are not used. If it is used, change the port number in both Unity and Python scripts. (For the Android app, it's the same)

## Importing Figma Designs to Unity

The design for the Unity project can be found [here](https://www.figma.com/design/OEFhAYB7VHAqsMBqTBMbz2/%E2%9D%A4%EF%B8%8F-%F0%9F%A7%A4-VTS-Gloves-Final-Design?node-id=683%3A8318&t=QioNSwJQcsjR3Gnb-1) and the report [here](https://docs.google.com/document/d/1UiRXvxPEOgVip0vOOhqs2Va40Fi3v0sDQcJMizoDwnw/edit#heading=h.eqd96iuoe4op).

Unfortunately, there are no good ways to import Figma designs. I tried unity figma bridge, which is somewhat useful for UI but not for game elements. Also the imported UI is blurry.

To import a design, you need to select elements, copy as svg, save svg, use the Assets/svg2png.py script to convert svg to png, and then import the assets to unity. To run the script, you need to install cairosvg and pillow in your python environment.

## Exporting the Unity Project to Android (Important! Otherwise, the Android app will not work)


### Step 1
In the Unity Editor, go to File > Build Profiles. Make sure the Android platform is selected and "Export Project" is checked. Click "Switch Platform" if necessary.

![Checklist 1](/screenshots%20and%20photos/setup/checklist1.png)


### Step 2

Click on "Player Settings in the Build Profiles window. Make sure the highlighted settings are set as shown in the screenshots below. (I believe they are already set like this, but it's good to double-check.)

![Checklist 2](/screenshots%20and%20photos/setup/checklist2.png)

### Step 3

Click on "Export" in the Build Profiles window. You must choose "AndroidBuild" as the export location. "AndroidBuild" should be at the same level as the "AndroidApp" and "UnityGame" folders. 

![Export](/screenshots%20and%20photos/setup/export.png)

### Step 4

After exporting, double-check that the files are in the correct location, as shown below.

![Export Location](/screenshots%20and%20photos/setup/export-location.png)

### Step 5

Open both the "gradle.properties" file in the "AndroidApp" folder and the "gradle.properties" file in the "AndroidBuild" folder. We need to modify several fields in the "gradle.properties" file in the "AndroidApp" folder. "AndroidApp" is the Android app that we will run on the phone. 

The fields that need to be modified are: unityProjectPath, unity.projectPath, unity.androidSdkPath, unity.androidNdkPath, unity.androidNdkVersion, unity.jdkPath.

Please copy the values from the "gradle.properties" file in the "AndroidBuild" folder to the "gradle.properties" file in the "AndroidApp" folder. Do not copy the entire file.

![Gradle Settings](/screenshots%20and%20photos/setup/gradle-properties.png)

### Step 6 (Optional) 

Since we do not want to export the Unity games as a dedicated Android app, but rather as a library that the Android app can use, we can go to the "AndroidBuild/unityLibrary/src/main" and modify the "AndroidManifest.xml" file. Remove the `<intent-filter>` tags from the `<activity>` tags. This will prevent icons for the standalone Unity games from being displayed in the app list. (If you rebuild the Unity project, you DO NOT need to do this step again.) The following screenshot shows what the "AndroidManifest.xml" file should look like after the modification.

![Android Manifest](/screenshots%20and%20photos/setup/manifest.png)

### Step 7

The Android app is now ready to be run on the phone. Open the "AndroidApp" folder in Android Studio and run the app on your phone (assume you have the developer mode enabled on your phone). If Android Studio asks you to choose which SDK to use (since Unity installs the SDK in a different location), choose the SDK that Unity uses (though it is unlikely that chooing the SDK that Android Studio uses will cause any issues). Building the app could take as long as 10 minutes in the first run, should only take a few seconds in subsequent runs.

## Future Work for Unity Project
* Add more games.
* Create scripts to control a rigged 3d hand model for better visualization.
* Investigate the Mediapipe Plugin for Unity for better performance.
* Connect game result data to clinically relevant metrics such as passive range of motion, Fugl-Meyer Assessment, etc.
* Make the games more responsive to different screen sizes and aspect ratios.
* Warn the user if the hand is not detected, or if the whole body is not within the camera view. (Mediapipe tracking results return a visibility score for the hand and the whole body.)

# Android App

The Android app contains both the legacy tests (finger tracking test, tactile sensation test, force sensing test, Modified Ashworth Test) and the new gamified tests which integrate the Unity project. 

The legacy tests mainly use built-in Android sensors (accelerometer, gyroscope, and magnetometer) to measure the user's hand movements, but they are not very easy to conduct. The finger tracking test uses the MediaPipe hand tracking model to detect finger poses, which inspired the new gamified tests.

The new gamified tests uses a foreground service that runs the MediaPipe hand tracking model to detect hand poses. The app sends the hand poses to the Unity Activity using UDP sockets. The Unity game will be displayed on the screen, and the user can interact with the game using their hands.

## Instructions

Carefully follow the instructions to export the Unity project to Android and run the Android app in the Android Studio. No other setup is required.

## Future Work

* Use a custom Mediapipe Graph to speed up the holistic tracking.
* Add a dashboard page.
* Add a user profile page.
* Have a better visualization page for the user's scores.
* Implement drawing lines on the screen to help visualize the Mediapipe hand and pose tracking results. (Unfortunately, there is not a "drawUtils" class in the Java and C# versions of Mediapipe, unlike the Python version.)

# Other Miscellaneous Files

There is a web app written in React and Next.js to allow for remote control of the Android app. This allows a clinician or caretaker to help the user navigate the app. For the front end, run npm install and npm run dev to start the web app. For the backend, install the dependencies in requirements.txt and run python app.py. The web app uses Socket.IO to communicate with the Android app. A docker-compose file is provided to run the backend in a container.

Note: the remote control feature does not work for the gamified tests yet.
