
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

    private float playerX = 0, playerY = 10, playerZ = 0;
    private float yaw = 0;
    private float velY = 0;
    private final float gravity = -0.01f;
    private final float moveSpeed = 0.1f;
    private final float jumpSpeed = 0.3f;

    private boolean showDebug = false;
    private long lastTime;
    private int frames;
    private int fps;

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
                if (key == GLFW_KEY_F3) {
                    showDebug = !showDebug;
                }
            } else if (action == GLFW_RELEASE) {
                keys.remove(key);
            }
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

        playerX += dx;
        playerZ += dz;

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

        glRotatef(10, 1, 0, 0);
        glRotatef(-yaw, 0, 1, 0);
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
        System.out.printf("FPS: %d | Position: (%.2f, %.2f, %.2f)%n", fps, playerX, playerY, playerZ);
    }

    public static void main(String[] args) {
        new Cube3D().run();
    }
}
