package learnOpenGL.a_gettingStarted

/**
 * Created by elect on 26/04/17.
 */

import glm_.*
import glm_.glm.cos
import glm_.glm.sin
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import learnOpenGL.common.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.EXTABGR
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_BGR
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL30.*
import uno.buffer.destroy
import uno.buffer.destroyBuffers
import uno.buffer.floatBufferOf
import uno.buffer.intBufferBig
import uno.glf.semantic
import uno.gln.*
import glm_.vec3.operators.times
import uno.glfw.GlfwWindow.Cursor.Disabled
import org.lwjgl.opengl.GL20.glGetUniformLocation
import uno.glfw.GlfwWindow
import uno.glfw.glfw
import uno.glsl.Program


fun main(args: Array<String>) {

    with(CameraMouseZoom()) {

        run()
        end()
    }
}

private class CameraMouseZoom {

    val window: GlfwWindow

    val program: ProgramA

    val vbo = intBufferBig(1)
    val vao = intBufferBig(1)

    val vertices = floatBufferOf(
            -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,
            +0.5f, -0.5f, -0.5f, 1.0f, 0.0f,
            +0.5f, +0.5f, -0.5f, 1.0f, 1.0f,
            +0.5f, +0.5f, -0.5f, 1.0f, 1.0f,
            -0.5f, +0.5f, -0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,

            -0.5f, -0.5f, +0.5f, 0.0f, 0.0f,
            +0.5f, -0.5f, +0.5f, 1.0f, 0.0f,
            +0.5f, +0.5f, +0.5f, 1.0f, 1.0f,
            +0.5f, +0.5f, +0.5f, 1.0f, 1.0f,
            -0.5f, +0.5f, +0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, +0.5f, 0.0f, 0.0f,

            -0.5f, +0.5f, +0.5f, 1.0f, 0.0f,
            -0.5f, +0.5f, -0.5f, 1.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, +0.5f, 0.0f, 0.0f,
            -0.5f, +0.5f, +0.5f, 1.0f, 0.0f,

            +0.5f, +0.5f, +0.5f, 1.0f, 0.0f,
            +0.5f, +0.5f, -0.5f, 1.0f, 1.0f,
            +0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            +0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            +0.5f, -0.5f, +0.5f, 0.0f, 0.0f,
            +0.5f, +0.5f, +0.5f, 1.0f, 0.0f,

            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            +0.5f, -0.5f, -0.5f, 1.0f, 1.0f,
            +0.5f, -0.5f, +0.5f, 1.0f, 0.0f,
            +0.5f, -0.5f, +0.5f, 1.0f, 0.0f,
            -0.5f, -0.5f, +0.5f, 0.0f, 0.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,

            -0.5f, +0.5f, -0.5f, 0.0f, 1.0f,
            +0.5f, +0.5f, -0.5f, 1.0f, 1.0f,
            +0.5f, +0.5f, +0.5f, 1.0f, 0.0f,
            +0.5f, +0.5f, +0.5f, 1.0f, 0.0f,
            -0.5f, +0.5f, +0.5f, 0.0f, 0.0f,
            -0.5f, +0.5f, -0.5f, 0.0f, 1.0f)

    // world space positions of our cubes
    val cubePositions = arrayOf(
            Vec3(0.0f, 0.0f, 0.0f),
            Vec3(2.0f, 5.0f, -15.0f),
            Vec3(-1.5f, -2.2f, -2.5f),
            Vec3(-3.8f, -2.0f, -12.3f),
            Vec3(2.4f, -0.4f, -3.5f),
            Vec3(-1.7f, 3.0f, -7.5f),
            Vec3(1.3f, -2.0f, -2.5f),
            Vec3(1.5f, 2.0f, -2.5f),
            Vec3(1.5f, 0.2f, -1.5f),
            Vec3(-1.3f, 1.0f, -1.5f))

    object Texture {
        val A = 0
        val B = 1
        val Max = 2
    }

    val textures = intBufferBig(Texture.Max)

    val semantic.sampler.DIFFUSE_A get() = 0
    val semantic.sampler.DIFFUSE_B get() = 1

    // camera
    var cameraPos = Vec3(0.0f, 0.0f, 3.0f)
    var cameraFront = Vec3(0.0f, 0.0f, -1.0f)
    val cameraUp = Vec3(0.0f, 1.0f, 0.0f)

    var firstMouse = true
    /*  yaw is initialized to -90.0 degrees since a yaw of 0.0 results in a direction vector pointing to the right so we
        initially rotate a bit to the left.     */
    var yaw = -90.0f
    var pitch = 0.0f
    var lastX = 800.0f / 2.0
    var lastY = 600.0 / 2.0
    var fov = 45.0f

    var deltaTime = 0.0f    // time between current frame and last frame
    var lastFrame = 0.0f

    init {

        with(glfw) {

            /*  Initialize GLFW. Most GLFW functions will not work before doing this.
                It also setups an error callback. The default implementation will print the error message in System.err.    */
            init()

            //  Configure GLFW
            windowHint {
                context.version = "3.3"
                profile = "core"
            }
        }

        //  glfw window creation
        window = GlfwWindow(800, 600, "Camera Mouse Zoom")

        with(window) {

            makeContextCurrent() // Make the OpenGL context current

            show()   // Make the window visible

            framebufferSizeCallback = this@CameraMouseZoom::framebuffer_size_callback
            cursorPosCallback = this@CameraMouseZoom::mouse_callback
            scrollCallback = this@CameraMouseZoom::scroll_callback

            // tell GLFW to capture our mouse
            cursor = Disabled
        }

        /* This line is critical for LWJGL's interoperation with GLFW's OpenGL context, or any context that is managed
           externally. LWJGL detects the context that is current in the current thread, creates the GLCapabilities instance
           and makes the OpenGL bindings available for use.    */
        GL.createCapabilities()


        // configure global opengl state
        glEnable(GL_DEPTH_TEST)


        // build and compile our shader program, you can name your shader files however you like
        program = ProgramA("shaders/a/_14", "camera")


        //  set up vertex data (and buffer(s)) and configure vertex attributes
        glGenVertexArrays(vao)
        glGenBuffers(vbo)

        //  bind the Vertex Array Object first, then bind and set vertex buffer(s), and then configure vertex attributes(s).
        glBindVertexArray(vao)

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        //  position attribute
        glVertexAttribPointer(semantic.attr.POSITION, Vec3.length, GL_FLOAT, false, Vec3.size + Vec2.size, 0)
        glEnableVertexAttribArray(semantic.attr.POSITION)
        // texture coord attribute
        glVertexAttribPointer(semantic.attr.TEX_COORD, Vec2.length, GL_FLOAT, false, Vec3.size + Vec2.size, Vec3.size)
        glEnableVertexAttribArray(semantic.attr.TEX_COORD)


        // load and create a texture
        glGenTextures(textures)

        //  texture A
        glBindTexture(GL_TEXTURE_2D, textures[Texture.A])
        //  set the texture wrapping parameters to GL_REPEAT (default wrapping method)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        // set texture filtering parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        // load image, create texture and generate mipmaps
        var image = readImage("textures/container.jpg").flipY()
        var data = image.toByteBuffer()

        glTexImage2D(GL_TEXTURE_2D, GL_RGB, image.width, image.height, GL_BGR, GL_UNSIGNED_BYTE, data)
        glGenerateMipmap(GL_TEXTURE_2D)

        data.destroy()


        //  texture B
        glBindTexture(GL_TEXTURE_2D, textures[Texture.B])
        //  set the texture wrapping parameters to GL_REPEAT (default wrapping method)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        // set texture filtering parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        // load image, create texture and generate mipmaps
        image = readImage("textures/awesomeface.png").flipY()
        data = image.toByteBuffer()

        glTexImage2D(GL_TEXTURE_2D, GL_RGB, image.width, image.height, EXTABGR.GL_ABGR_EXT, GL_UNSIGNED_BYTE, data)
        glGenerateMipmap(GL_TEXTURE_2D)

        data.destroy()


        /*  You can unbind the VAO afterwards so other VAO calls won't accidentally modify this VAO, but this rarely happens.
            Modifying other VAOs requires a call to glBindVertexArray anyways so we generally don't unbind VAOs (nor VBOs)
            when it's not directly necessary.   */
        //glBindVertexArray()
    }

    inner class ProgramA(root: String, shader: String) : Program(root, "$shader.vert", "$shader.frag") {

        val model = glGetUniformLocation(name, "model")
        val view = glGetUniformLocation(name, "view")
        val proj = glGetUniformLocation(name, "projection")

        init {
            /*  Tell opengl for each sampler to which texture unit it belongs to (only has to be done once)
            Code passed to usingProgram() {..] is executed using the given program, which at the end gets unbound   */
            usingProgram(name) {
                "textureA".unit = semantic.sampler.DIFFUSE_A
                "textureB".unit = semantic.sampler.DIFFUSE_B
            }
        }
    }

    fun run() {

        //  render loop
        while (window.open) {

            // per-frame time logic
            val currentFrame = glfw.time
            deltaTime = currentFrame - lastFrame
            lastFrame = currentFrame

            //  input
            processInput(window)

            //  render
            glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // also clear the depth buffer now!

            //  bind textures on corresponding texture units
            glActiveTexture(GL_TEXTURE0 + semantic.sampler.DIFFUSE_A)
            glBindTexture(GL_TEXTURE_2D, textures[Texture.A])
            glActiveTexture(GL_TEXTURE0 + semantic.sampler.DIFFUSE_B)
            glBindTexture(GL_TEXTURE_2D, textures[Texture.B])

            usingProgram(program) {

                // pass projection matrix to shader (note that in this case it could change every frame)
                glm.perspective(fov.rad, window.aspect, 0.1f, 100.0f) to program.proj

                // camera/view transformation
                glm.lookAt(cameraPos, cameraPos + cameraFront, cameraUp) to program.view

                // render boxes
                glBindVertexArray(vao)
                cubePositions.forEachIndexed { i, vec3 ->

                    // calculate the model matrix for each object and pass it to shader before drawing
                    val model = Mat4() translate_ vec3
                    val angle = 20.0f * i
                    model.rotate_(angle.rad, 1.0f, 0.3f, 0.5f)
                    model to program.model

                    glDrawArrays(GL_TRIANGLES, 36)
                }
            }

            //  glfw: swap buffers and poll IO events (keys pressed/released, mouse moved etc.)
            window.swapBuffers()
            glfw.pollEvents()
        }
    }

    fun end() {

        //  optional: de-allocate all resources once they've outlived their purpose:
        glDeleteProgram(program)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)
        glDeleteTextures(textures)

        destroyBuffers(vao, vbo, textures, vertices)

        window.destroy()
        //  glfw: terminate, clearing all previously allocated GLFW resources.
        glfw.terminate()
    }

    /** process all input: query GLFW whether relevant keys are pressed/released this frame and react accordingly   */
    fun processInput(window: GlfwWindow) {

        if (window.pressed(GLFW_KEY_ESCAPE))
            window.close = true

        val cameraSpeed = 2.5 * deltaTime
        if (window.pressed(GLFW_KEY_W))
            cameraPos += cameraSpeed * cameraFront
        if (window.pressed(GLFW_KEY_S))
            cameraPos -= cameraSpeed * cameraFront
        if (window.pressed(GLFW_KEY_A))
            cameraPos -= glm.normalize(glm.cross(cameraFront, cameraUp)) * cameraSpeed  // glm classic
        if (window.pressed(GLFW_KEY_D))
            cameraPos += (cameraFront cross cameraUp).normalize_() * cameraSpeed    // glm enhanced

        // TODO up/down?
    }

    /** glfw: whenever the window size changed (by OS or user resize) this callback function executes   */
    fun framebuffer_size_callback(width: Int, height: Int) {

        /*  make sure the viewport matches the new window dimensions; note that width and height will be significantly
            larger than specified on retina displays.     */
        glViewport(0, 0, width, height)
    }

    /** glfw: whenever the mouse moves, this callback is called */
    fun mouse_callback(xpos: Double, ypos: Double) {

        if (firstMouse) {
            lastX = xpos
            lastY = ypos
            firstMouse = false
        }

        var xoffset = xpos - lastX
        var yoffset = lastY - ypos // reversed since y-coordinates go from bottom to top
        lastX = xpos
        lastY = ypos

        val sensitivity = 0.1f // change this value to your liking
        xoffset *= sensitivity
        yoffset *= sensitivity

        yaw += xoffset.f
        pitch += yoffset.f

        // make sure that when pitch is out of bounds, screen doesn't get flipped
        if (pitch > 89.0f)
            pitch = 89.0f
        if (pitch < -89.0f)
            pitch = -89.0f

        val front = Vec3(
                x = cos(glm.radians(yaw)) * cos(glm.radians(pitch)), // classic glm
                y = sin(pitch.rad), // one glm alternative
                z = yaw.rad.sin * pitch.rad.cos)                        // another glm alternative
        cameraFront = front.normalize_()
    }

    /** glfw: whenever the mouse scroll wheel scrolls, this callback is called  */
    fun scroll_callback(xOffset: Double, yOffset: Double) {
        if (fov in 1.0f..45.0f)
            fov -= yOffset.f
        fov = glm.clamp(fov, 1.0f, 45.0f)
    }
}