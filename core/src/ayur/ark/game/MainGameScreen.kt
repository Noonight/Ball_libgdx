package ayur.ark.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import sun.misc.Signal.handle

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import java.util.*;


class MainGameScreen(val game: Ball) : Screen {
    lateinit var stage: Stage
    lateinit var world: World
    lateinit var ball: Image
    lateinit var paddle: Image
    lateinit var ballBody: Body
    lateinit var wallBody: Body
    lateinit var floorBody: Body
    lateinit var paddleBody: Body
    lateinit var blocks: HashMap<Body, Image>
    var scale = 32;

    override fun show() {
        stage = Stage();
        blocks = HashMap();

        val ballTexture = Texture("ball.png")
        val blockTexture = Texture("block.png")

        ball = Image(ballTexture);
        stage.addActor(ball);
        paddle = Image(blockTexture);
        stage.addActor(paddle);

        val screenWidth = Gdx.graphics.getWidth();
        val screenHeight = Gdx.graphics.getHeight();
        //val screenWidth = 800;
        //val screenHeight = 480;

        val blockWidth = blockTexture.getWidth();
        val blockHeight = blockTexture.getHeight();

        world = World(Vector2(0f, 0f), true);

        var col = screenWidth / blockWidth;
        while (col > 0) {
            col--;

            var row = screenHeight / blockHeight / 2;
            while (row > 0) {
                row--;

                var x = col * blockWidth;
                var y = row * blockHeight + screenHeight / 2;

                val block = Image(blockTexture);
                block.setPosition(x.toFloat(), y.toFloat());
                stage.addActor(block);
                val blockBody = createRectBody(x.toFloat(), y.toFloat(), blockWidth.toFloat(), blockHeight.toFloat());
                val put = blocks.put(blockBody, block);
            }
        }

        ballBody = createBallBody(10f, 10f, (ballTexture.getWidth() / 2).toFloat());
        ballBody.setLinearVelocity(10f, 10f);

        wallBody = createRectBody(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat());
        floorBody = createRectBody(0f, 0f, screenWidth.toFloat(), 1f);
        paddleBody = createRectBody(0f, 0f, blockWidth.toFloat(), blockHeight.toFloat());

        Gdx.input.setInputProcessor(stage);
        stage.addListener(object : InputListener() {
            override fun handle(e: Event?): Boolean {
                var x = Gdx.input.getX() - blockWidth / 2
                moveBody(paddleBody, x.toFloat(), 0f)
                return true
            }
        })

        world.setContactListener(object : ContactListener {

            override fun beginContact(contact: Contact?) {
                var b = contact?.fixtureA?.body
                var i: Image? = blocks.get(b)
                if (i != null) {
                    i.remove()
                } else if (b == floorBody) {
                    show()
                }
            }

            override fun endContact(contact: Contact?) {}
            override fun preSolve(contact: Contact?, oldManifold: Manifold?) {}
            override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {}
        })
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        world.step(delta, 10, 10);
        paddle.setPosition(paddleBody.getPosition().x * scale, paddleBody.getPosition().y * scale);
        ball.setPosition(ballBody.getPosition().x * scale, ballBody.getPosition().y * scale);

        val iter = blocks.keys.iterator()
        //val iter = blocks.keySet().iterator();
        while (iter.hasNext()) {
            var b = iter.next();
            var i = blocks.get(b);
            if (i!!.hasParent() == false) {
                world.destroyBody(b);
                iter.remove();
            }
        }

        stage.act(delta);
        stage.draw();
    }

    override fun dispose() {
    }

    override fun hide() {
    }

    override fun pause() {
    }

    override fun resize(width: Int, height: Int) {
    }

    override fun resume() {
    }

    fun createBallBody(x: Float, y: Float, radius: Float): Body {
        var x = x / scale;
        var y = y / scale;
        var radius = radius / scale;

        var def: BodyDef = BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.position.set(x, y);
        var body: Body = world.createBody(def);

        var shape: CircleShape = CircleShape();
        shape.setRadius(radius);
        shape.setPosition(Vector2(radius, radius));
        var fixture: Fixture = body.createFixture(shape, 1f);
        fixture.setFriction(0f);
        fixture.setRestitution(1f);
        shape.dispose();

        return body;
    }

    fun createRectBody(x: Float, y: Float, width: Float, height: Float): Body {
        var x = x / scale;
        var y = y / scale;
        var width = width / scale;
        var height = height / scale;

        var def: BodyDef = BodyDef();
        def.type = BodyDef.BodyType.StaticBody;
        def.position.set(x, y);
        var body: Body = world.createBody(def);

        var shape: ChainShape = ChainShape();
        var vertices = floatArrayOf(
                0f, 0f,
                0f, height,
                width, height,
                width, 0f,
                0f, 0f
        )
        /*float[] vertices = {
            0, 0,
            0, height,
            width, height,
            width, 0,
            0, 0
        };*/

        shape.createChain(vertices);
        body.createFixture(shape, 1f);
        shape.dispose();

        return body;
    }

    fun moveBody(body: Body, x: Float, y: Float) {
        var x = x / scale;
        var y = y / scale;

        body.setTransform(x, y, 0f);
    }
}