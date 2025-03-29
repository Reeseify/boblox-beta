
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
    private int terrainList;
    private final int worldSize = 30;

    // Camera
    private float camX = 0, camY = 5, camZ = 5;
    private float pitch = 0, yaw = 0;
    private final float speed = 0.1f;
    private final float mouseSensitivity = 0.1f;
    private double lastMouseX, lastMouseY;
    private boolean firstMouse = true;

    private final Set<Integer> keys = new HashSet<>();

    public void run() {
        init();
        loop();
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        window = glfwCreateWindow(800, 600, "3D World", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Failed to create GLFW window");

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        glfwShowWindow(window);
        GL.createCapabilities();

        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) keys.add(key);
            else if (action == GLFW_RELEASE) keys.remove(key);
        });

        glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            if (firstMouse) {
                lastMouseX = xpos;
                lastMouseY = ypos;
                firstMouse = false;
            }

            double xoffset = xpos - lastMouseX;
            double yoffset = lastMouseY - ypos;
            lastMouseX = xpos;
            lastMouseY = ypos;

            xoffset *= mouseSensitivity;
            yoffset *= mouseSensitivity;

            yaw += xoffset;
            pitch += yoffset;

            pitch = Math.max(-89.0f, Math.min(89.0f, pitch));
        });

        createTerrainDisplayList();
    }

    private void createTerrainDisplayList() {
        terrainList = glGenLists(1);
        glNewList(terrainList, GL_COMPILE);
        for (int x = -worldSize / 2; x < worldSize / 2; x++) {
            for (int z = -worldSize / 2; z < worldSize / 2; z++) {
                glPushMatrix();
                glTranslatef(x, 0, z);
                drawCube();
                glPopMatrix();
            }
        }
        glEndList();
    }

    private void processInput() {
        float dx = 0, dy = 0, dz = 0;

        float cosYaw = (float) Math.cos(Math.toRadians(yaw));
        float sinYaw = (float) Math.sin(Math.toRadians(yaw));

        if (keys.contains(GLFW_KEY_W)) {
            dx += -sinYaw * speed;
            dz += -cosYaw * speed;
        }
        if (keys.contains(GLFW_KEY_S)) {
            dx += sinYaw * speed;
            dz += cosYaw * speed;
        }
        if (keys.contains(GLFW_KEY_A)) {
            dx += -cosYaw * speed;
            dz += sinYaw * speed;
        }
        if (keys.contains(GLFW_KEY_D)) {
            dx += cosYaw * speed;
            dz += -sinYaw * speed;
        }
        if (keys.contains(GLFW_KEY_SPACE)) dy += speed;
        if (keys.contains(GLFW_KEY_LEFT_SHIFT)) dy -= speed;

        camX += dx;
        camY += dy;
        camZ += dz;
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glEnable(GL_DEPTH_TEST);

            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            float aspect = 800f / 600f;
            glFrustum(-aspect, aspect, -1, 1, 1, 100);

            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();
            applyCamera();

            glCallList(terrainList);

            glfwSwapBuffers(window);
            glfwPollEvents();
            processInput();
        }
    }

    private void applyCamera() {
        glRotatef(pitch, 1, 0, 0);
        glRotatef(yaw, 0, 1, 0);
        glTranslatef(-camX, -camY, -camZ);
    }

    private void drawCube() {
        glBegin(GL_QUADS);

        glColor3f(0.4f, 0.8f, 0.4f);
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

    public static void main(String[] args) {
        new Cube3D().run();
    }
}
