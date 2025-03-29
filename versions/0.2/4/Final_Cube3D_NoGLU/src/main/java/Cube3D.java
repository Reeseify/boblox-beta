
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Cube3D {
    private long window;
    private final Set<Integer> keys = new HashSet<>();
    private boolean holdingRightClick = false;

    private float playerX = 0, playerY = 1, playerZ = 0;
    private float yaw = 0, pitch = 20;
    private float velY = 0;
    private final float gravity = -0.01f;
    private final float moveSpeed = 0.1f;
    private final float jumpSpeed = 0.25f;

    private float cameraDistance = 6.0f;
    private boolean showDebug = false;
    private long lastTime;
    private int frames, fps;

    private double lastMouseX, lastMouseY;
    private boolean firstMouse = true;

    public void run() {
        init();
        loop();
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        window = glfwCreateWindow(800, 600, "Roblox-style Java Engine", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Failed to create GLFW window");

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
        GL.createCapabilities();

        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                keys.add(key);
                if (key == GLFW_KEY_F3) showDebug = !showDebug;
            } else if (action == GLFW_RELEASE) {
                keys.remove(key);
            }
        });

        glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_RIGHT) {
                holdingRightClick = (action == GLFW_PRESS);
                if (holdingRightClick) firstMouse = true;
            }
        });

        glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            if (!holdingRightClick) return;

            if (firstMouse) {
                lastMouseX = xpos;
                lastMouseY = ypos;
                firstMouse = false;
            }

            double dx = xpos - lastMouseX;
            double dy = lastMouseY - ypos;
            lastMouseX = xpos;
            lastMouseY = ypos;

            yaw += dx * 0.2f;
            pitch += dy * 0.2f;
            pitch = Math.max(-89.0f, Math.min(89.0f, pitch));
        });

        glfwSetScrollCallback(window, (win, xoffset, yoffset) -> {
            cameraDistance -= yoffset;
            cameraDistance = Math.max(2.0f, Math.min(20.0f, cameraDistance));
        });

        lastTime = System.currentTimeMillis();
    }

    private void processInput() {
        float dx = 0, dz = 0;

        float rad = (float) Math.toRadians(yaw);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);

        if (keys.contains(GLFW_KEY_W)) {
            dx += -sin * moveSpeed;
            dz += -cos * moveSpeed;
        }
        if (keys.contains(GLFW_KEY_S)) {
            dx += sin * moveSpeed;
            dz += cos * moveSpeed;
        }
        if (keys.contains(GLFW_KEY_A)) {
            dx += -cos * moveSpeed;
            dz += sin * moveSpeed;
        }
        if (keys.contains(GLFW_KEY_D)) {
            dx += cos * moveSpeed;
            dz += -sin * moveSpeed;
        }

        float nextX = playerX + dx;
        float nextZ = playerZ + dz;

        if (Math.abs(nextX) < 49 && Math.abs(nextZ) < 49) {
            playerX = nextX;
            playerZ = nextZ;
        }

        if (playerY <= 1.0f) {
            velY = 0;
            playerY = 1.0f;
            if (keys.contains(GLFW_KEY_SPACE)) velY = jumpSpeed;
        } else {
            velY += gravity;
        }

        playerY += velY;
    }

    private void updateFPS() {
        frames++;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime >= 1000) {
            fps = frames;
            frames = 0;
            lastTime = currentTime;
        }
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            updateFPS();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glEnable(GL_DEPTH_TEST);

            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            float aspect = 800f / 600f;
            glFrustum(-aspect, aspect, -1, 1, 1, 100);

            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();
            applyCamera();

            drawBaseplate();
            drawPlayer();
            if (showDebug) drawF3Overlay();

            glfwSwapBuffers(window);
            glfwPollEvents();
            processInput();
        }
    }

    private void applyCamera() {
        float camX = playerX + (float)(Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch))) * cameraDistance;
        float camY = playerY + (float)(Math.sin(Math.toRadians(pitch))) * cameraDistance + 1.5f;
        float camZ = playerZ + (float)(Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch))) * cameraDistance;

        
    // Manually apply camera transforms to simulate gluLookAt
    glRotatef(pitch, 1, 0, 0);
    glRotatef(yaw, 0, 1, 0);
    glTranslatef(-playerX, -playerY - 1.5f, -playerZ - cameraDistance);
    
    }

    private void drawBaseplate() {
        glPushMatrix();
        glColor3f(0.5f, 0.5f, 0.5f);
        glTranslatef(0, 0, 0);
        glScalef(100, 1, 100);
        drawCube();
        glPopMatrix();
    }

    private void drawPlayer() {
        glPushMatrix();
        glColor3f(1, 1, 0);
        glTranslatef(playerX, playerY, playerZ);
        drawCube();
        glPopMatrix();
    }

    private void drawCube() {
        glBegin(GL_QUADS);
        // Top
        glVertex3f(-0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        // Bottom
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(-0.5f, -0.5f, 0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);
        glVertex3f(0.5f, -0.5f, -0.5f);
        // Front
        glVertex3f(-0.5f, -0.5f, 0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        // Back
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(-0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, -0.5f);
        // Left
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(-0.5f, -0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, -0.5f);
        // Right
        glVertex3f(0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);
        glEnd();
    }

    private void drawF3Overlay() {
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, 800, 600, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glColor3f(1, 1, 1);
        glRasterPos2i(10, 20);
        for (char c : ("FPS: " + fps).toCharArray()) {
            glutBitmapCharacter(c);
        }

        glRasterPos2i(10, 40);
        for (char c : String.format("Pos: (%.2f, %.2f, %.2f)", playerX, playerY, playerZ).toCharArray()) {
            glutBitmapCharacter(c);
        }

        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
    }

    private void glutBitmapCharacter(char c) {
        // Basic fixed width debug char (placeholder)
        glBegin(GL_POINTS);
        for (int i = 0; i < 5; i++) {
            glVertex2i(10 + i, 0);
        }
        glEnd();
    }

    public static void main(String[] args) {
        new Cube3D().run();
    }
}
