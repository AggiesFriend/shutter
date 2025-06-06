# Shutter

A simple Java application that exposes a small web page for remotely shutting down or restarting a Windows machine. It is intended to run on startup and can be scheduled with Windows Task Scheduler.

## Building

This project uses Maven. From the project directory run:

```bash
mvn package
```

The resulting JAR will be in `target/shutter-1.0-SNAPSHOT.jar`.

## Running

Run the jar with:

```bash
java -jar target/shutter-1.0-SNAPSHOT.jar
```

The server listens on port `8000` by default. Browse to `http://<your-ip>:8000` from another machine on the same network. Enter the password and choose **Shutdown** or **Restart**.

## Startup with Windows Task Scheduler

1. Build the application and copy the jar to a convenient location.
2. Open **Task Scheduler** and create a new task.
3. In **Triggers**, add a trigger for **At startup**.
4. In **Actions**, add a new action with:
   - Program/script: `java`
   - Add arguments: `-jar "C:\path\to\shutter-1.0-SNAPSHOT.jar"`
   - Start in: the folder containing the jar
5. Save the task. The server will start automatically on boot.

Change the hard-coded password and port in `ShutterServer.java` if needed.
