# Android Real-Time OpenCV Processor

This project is a technical assessment for the Software Engineering Intern (R&D) role. It demonstrates a complete pipeline for capturing frames from the Android camera, processing them in real-time with OpenCV in C++, and displaying the result.

## 🚀 Core Features

### Android Application

- **Real-Time Camera Feed:** Uses **CameraX** (`ImageAnalysis` use case) to get a high-framerate, real-time image stream from the device camera.
- **JNI / NDK Integration:** A robust C++ bridge built with JNI (Java Native Interface) to pass camera frames from Kotlin to native C++.
- **C++ OpenCV Processing:** All image processing is done in C++ for maximum performance.
  - [cite_start]**Canny Edge Detection:** Applies the Canny algorithm to each frame to detect edges[cite: 27].
- **Efficient Display:** The processed frames are converted to Bitmaps and displayed in an `ImageView`, demonstrating the full processing pipeline.
- **Permissions Handling:** Correctly requests and verifies `android.permission.CAMERA` before starting the camera.

### Web Viewer (Debug Tool)

- [cite_start]**TypeScript + HTML:** A minimal web viewer built with TypeScript and HTML, as required[cite: 13, 18].
- [cite_start]**Static Frame Display:** Shows a sample processed Canny frame from the Android app[cite: 37].
- [cite_start]**DOM Manipulation:** Uses TypeScript to dynamically update the DOM with mock frame statistics (resolution, FPS)[cite: 38, 39].
- [cite_start]**Buildable:** Includes a `tsconfig.json` and is buildable via the `tsc` compiler[cite: 50].

## 📸 Screenshots & Demo

![Demo of Canny edge detection on Android](screenshots\android-demo-1.png)
_A screenshot of the Canny edge detection running on the device._

![Demo of the TypeScript web viewer](screenshots\web-demo.png)
_A screenshot of the `/web` component running in a browser._

## 🏛️ Project Architecture

[cite_start]The project is structured to separate concerns, as requested in the guidelines[cite: 41].

1.  **`/app` (Kotlin/Java):**

    - `MainActivity.kt`: Manages the camera, requests permissions, and sets up the `ImageAnalysis` pipeline.
    - The `ImageAnalysis.Analyzer` sends each `ImageProxy` to the C++ layer via JNI.
    - It receives the processed `ByteArray` from C++, converts it to a `Bitmap`, and displays it in the `ImageView`.

2.  **`/app/src/main/cpp` (C++ & JNI):**

    - `CMakeLists.txt`: Configures the NDK build, links the `androidopencvproject` library against `libopencv_java4.so`, and handles dependencies like `c++_shared`.
    - `native-lib.cpp`: Contains the JNI "bridge" function (`Java_com_example_androidopencvproject_MainActivity_processImage`). This function:
      1.  Converts the Java `jbyteArray` (from the Y-plane) into a C++ `cv::Mat`.
      2.  [cite_start]Applies `cv::Canny()` to the `Mat`[cite: 27].
      3.  Encodes the resulting edge-detected `Mat` into a JPEG.
      4.  Returns the data as a `jbyteArray` back to Kotlin.

3.  **`/opencv` (Local Dependency):**

    - Contains the pre-compiled OpenCV Android SDK (`.so` libraries and C++ headers).
    - The `CMakeLists.txt` file is configured to find this library using a relative path, making the project portable.

4.  **`/web` (TypeScript):**
    - `index.html`: The HTML structure.
    - `app.ts`: TypeScript code for DOM manipulation.
    - `tsconfig.json`: Configuration for the TypeScript compiler.
    - `sample_canny.jpg`: A static sample image for the viewer.

## 🛠️ Setup & Build Instructions

### 1. Android App

1.  **Clone the Repository:**
    ```bash
    git clone https://github.com/Wizard-Mayank/Android-OpenCV-Project.git
    cd AndroidOpenCVProject
    ```
2.  **Open in Android Studio:** Open the `AndroidOpenCVProject` folder in Android Studio.
3.  **Sync Gradle:** The project is configured with a local `opencv` folder. Gradle should sync automatically.
4.  **Build:** The NDK/CMake build will be triggered, compiling the `native-lib.cpp` and linking all libraries.
5.  **Run:** Run the `app` configuration on a physical Android device.

### 2. Web Viewer

1.  **Install TypeScript** (if not already installed):
    ```bash
    npm install -g typescript
    ```
2.  **Navigate to the web folder:**
    ```bash
    cd web
    ```
3.  **Compile the TypeScript:**
    ```bash
    tsc
    ```
    This will create an `app.js` file.
4.  **View:** Open `index.html` in any web browser.
