
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.DoubleBuffer;
import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Cube3D {
    private long window;
    private final Set<Integer> keys = new HashSet<>();
    private boolean holdingRightClick = false;

    private float playerX = 0, playerY = 10, playerZ = 0;
    private float yaw = 0, pitch = 10;
    private float velY = 0;
    private final float gravity = -0.01f;
    private final float moveSpeed = 0.1f;
    private final float jumpSpeed = 0.3f;

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

        lastTime = System.currentTimeMillis();
    }

    private void processInput() {
        float dx = 0, dz = 0;

        float radYaw = (float) Math.toRadians(yaw);
        float cosYaw = (float) Math.cos(radYaw);
        float sinYaw = (float) Math.sin(radYaw);

        if (keys.contains(GLFW_KEY_W)) {
            dx += -sinYaw * moveSpeed;
            dz += -cosYaw * moveSpeed;
        }
        if (keys.contains(GLFW_KEY_S)) {
            dx += sinYaw * moveSpeed;
            dz += cosYaw * moveSpeed;
        }
        if (keys.contains(GLFW_KEY_A)) {
            dx += -cosYaw * moveSpeed;
            dz += sinYaw * moveSpeed;
        }
        if (keys.contains(GLFW_KEY_D)) {
            dx += cosYaw * moveSpeed;
            dz += -sinYaw * moveSpeed;
        }

        float nextX = playerX + dx;
        float nextZ = playerZ + dz;

        // Collision with edge of baseplate (size 100)
        if (Math.abs(nextX) < 50 && Math.abs(nextZ) < 50) {
            playerX = nextX;
            playerZ = nextZ;
        }

        if (playerY <= 0.5f) {
            velY = 0;
            playerY = 0.5f;
            if (keys.contains(GLFW_KEY_SPACE)) {
                velY = jumpSpeed;
            }
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
            if (showDebug) drawDebugOverlay();

            glfwSwapBuffers(window);
            glfwPollEvents();
            processInput();
        }
    }

    private void applyCamera() {
        float camX = playerX;
        float camY = playerY + 2;
        float camZ = playerZ + 5;

        glRotatef(pitch, 1, 0, 0);
        glRotatef(yaw, 0, 1, 0);
        glTranslatef(-camX, -camY, -camZ);
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

    private void drawDebugOverlay() {
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, 800, 600, 0, -1, 1);

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glColor3f(1, 1, 1);
        glRasterPos2i(10, 20);
        glutBitmapString(String.format("FPS: %d", fps));

        glRasterPos2i(10, 40);
        glutBitmapString(String.format("Pos: (%.2f, %.2f, %.2f)", playerX, playerY, playerZ));

        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
    }

    private void glutBitmapString(String text) {
        for (char c : text.toCharArray()) {
            glutBitmapCharacter(c);
        }
    }

    private void glutBitmapCharacter(char c) {
        for (int i = 0; i < 8; i++) {
            glBegin(GL_LINES);
            glVertex2f(10 + i, 0);
            glVertex2f(10 + i, 8);
            glEnd();
        }
    }

    public static void main(String[] args) {
        new Cube3D().run();
    }
}
