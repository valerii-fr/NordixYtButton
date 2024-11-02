# Project: Integration of a Physical Button with YouTrack Timer

## Description

This project consists of two components:

1. **Plugin for IntelliJ IDEA**: This extension integrates support for a connected physical button to control the timer of the YouTrack plugin. It provides reliable interaction between the physical button and the software, allowing users to easily manage the time spent on tasks.

2. **Arduino Firmware**: Implements the functionality of the physical button based on ESP32-S2. This firmware discovers the published service via mDNS and connects to the WSS server to manage the plugin. It supports different button actions such as single press, double press, and long press, each initiating a corresponding action on the plugin side.

## Components

### Plugin for IntelliJ IDEA

- **WebSocket Connection**: The plugin establishes a WebSocket connection with the button, allowing commands to start, stop, and pause the YouTrack timer in real-time.
- **State Management**: The plugin monitors the state of the timer and controls the LED on the button, turning it on or off depending on the current status (running/stopped).
- **User Convenience**: The intuitive interface and ease of management make the plugin an ideal tool for YouTrack users.

### Arduino Firmware (ESP32-S2)

- **Pins**:
    - Button: **GPIO 0**
    - LED: **GPIO 15**
- **Automatic Wi-Fi Setup**: Utilizes the `WiFiManager` library, allowing automatic configuration of Wi-Fi connection without the need to specify SSID and password in the code. This greatly simplifies the setup process and makes the device more user-friendly.
- **Action Handling**: The firmware supports various button actions (single press, double press, long press) and sends corresponding commands via WebSocket. This ensures a high degree of interactivity and allows users to control their workflow.

## Installation

1. **For the Plugin**:
    - Clone the repository and import it into IntelliJ IDEA.
    - Ensure that all dependencies are installed.
    - Run the plugin in your development environment following the setup instructions.

2. **For the Firmware**:
    - Install the ESP32 library in the Arduino IDE.
    - Copy the code into the Arduino IDE and upload it to your ESP32-S2 board.
    - Make sure the board is connected to your computer and the correct configuration is selected in the Arduino IDE.

## Usage

- Connect the button to the ESP32-S2 board and upload the firmware.
- Launch the plugin in IntelliJ IDEA.
- Once the button is connected, the state will automatically update on the server, allowing you to manage the YouTrack timer with the physical button.
- Use the button actions to control your workflow without distracting from tasks.

## Acknowledgments

Special thanks to the community for valuable resources and support throughout the project. Your efforts, ideas, and inspiration were invaluable in the realization of this integration. Thanks to everyone who shares their knowledge and experience — without you, this project would not have been possible. We hope that our work helps others in their projects and inspires new ideas!
