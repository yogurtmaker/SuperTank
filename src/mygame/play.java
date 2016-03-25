/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;


import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.HeightfieldCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FogFilter;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;
import com.jme3.terrain.geomipmap.TerrainGrid;
import com.jme3.terrain.geomipmap.TerrainGridListener;
import com.jme3.terrain.geomipmap.TerrainGridLodControl;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.geomipmap.grid.FractalTileLoader;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.terrain.noise.ShaderUtils;
import com.jme3.terrain.noise.basis.FilteredBasis;
import com.jme3.terrain.noise.filter.IterativeFilter;
import com.jme3.terrain.noise.filter.OptimizedErode;
import com.jme3.terrain.noise.filter.PerturbFilter;
import com.jme3.terrain.noise.filter.SmoothFilter;
import com.jme3.terrain.noise.fractal.FractalSum;
import com.jme3.terrain.noise.modulator.NoiseModulator;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;
import java.awt.Dimension;
import java.awt.Toolkit;

public class play extends SimpleApplication implements ActionListener {

  
    static Dimension screen;
    private BulletAppState bulletAppState;
    private Node model;
    private CharacterControl player;
    private CameraNode camNode;
    boolean rotate = false;
    private Vector3f walkDirection = new Vector3f(0, 0, 0);
    private Vector3f viewDirection = new Vector3f(0, 0, 0);
    private boolean forward = false, backward = false, leftRotate = false, rightRotate = false;
    private float airTime = 0;
    private FractalSum base;
    private PerturbFilter perturb;
    private OptimizedErode therm;
    private SmoothFilter smooth;
    private IterativeFilter iterate;
    private Material mat_terrain;
    private TerrainGrid terrain;

    public static void main(final String[] args) {
        play app = new play();
        initAppScreen(app);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        setUpKeys();
        processor();
        initTerrain();
        createCharacter();
        initPhysics();
        initCam();
    }

    private static void initAppScreen(SimpleApplication app) {
        AppSettings aps = new AppSettings(true);
        screen = Toolkit.getDefaultToolkit().getScreenSize();
        screen.width *= 0.75;
        screen.height *= 0.75;
        aps.setResolution(screen.width, screen.height);
        app.setSettings(aps);
        app.setShowSettings(false);
    }

    @Override
    public void simpleUpdate(final float tpf) {
        Vector3f camDir = cam.getDirection().mult(0.2f);
        Vector3f camLeft = cam.getLeft().mult(0.2f);
        Quaternion rotLeft = new Quaternion().fromAngles(0, 0, -FastMath.PI * tpf/4);
        Quaternion rotRight = new Quaternion().fromAngles(0, 0, FastMath.PI * tpf/4);
        Quaternion resetRot = new Quaternion().fromAngles(0, 0, 0);
        Quaternion limLeft = new Quaternion().fromAngles(0, 0, -FastMath.PI / 4);
        Quaternion limRight = new Quaternion().fromAngles(0, 0, FastMath.PI / 4);

        camDir.y = 0;
        camLeft.y = 0;
        viewDirection.set(camDir);
        walkDirection.set(0, 0, 0);
        if (forward) {
            walkDirection.addLocal(camDir.mult(5f));
            if (leftRotate) {
                viewDirection.addLocal(camLeft.mult(0.0015f));
                if (model.getChild(0).getLocalRotation().getZ() >= limLeft.getZ()) {
                    model.getChild(0).rotate(rotLeft);
                }
            } else if (rightRotate) {
                viewDirection.addLocal(camLeft.mult(0.0015f).negate());
                if (model.getChild(0).getLocalRotation().getZ() <= limRight.getZ()) {
                    model.getChild(0).rotate(rotRight);
                }
            } else {
                if (model.getChild(0).getLocalRotation() != resetRot){
                    
                }
            }
        } else if (backward) {
            walkDirection.addLocal(camDir.mult(5f).negate());
            if (leftRotate) {
                viewDirection.addLocal(camLeft.mult(0.0015f).negate());
                if (model.getChild(0).getLocalRotation().getZ() >= limLeft.getZ()) {
                    model.getChild(0).rotate(rotLeft);
                }
            } else if (rightRotate) {
                viewDirection.addLocal(camLeft.mult(0.0015f));
                if (model.getChild(0).getLocalRotation().getZ() <= limRight.getZ()) {
                    model.getChild(0).rotate(rotRight);
                }
            } else {
                if (model.getChild(0).getLocalRotation() != resetRot){
                    
                }
            }
        }
        player.setWalkDirection(walkDirection);
        player.setViewDirection(viewDirection);

//        if (walkDirection.length() == 0) {
//            if (!"stand".equals(animationChannel.getAnimationName())) {
//                animationChannel.setAnim("stand", 1f);
//            }
//        } else {
//            viewDirection.set(camDir);
//            if (airTime > .3f) {
//                if (!"stand".equals(animationChannel.getAnimationName())) {
//                    animationChannel.setAnim("stand");
//                }
//            } else if (!"Walk".equals(animationChannel.getAnimationName())) {
//                animationChannel.setAnim("Walk", 0.7f);
//            }
//        }
    }

    private void initTerrain() {
        this.mat_terrain = new Material(this.assetManager, "Common/MatDefs/Terrain/HeightBasedTerrain.j3md");

        Texture dirt = this.assetManager.loadTexture("Textures/dirt.jpg");
        dirt.setWrap(WrapMode.Repeat);
        this.mat_terrain.setTexture("region1ColorMap", dirt);
        this.mat_terrain.setVector3("region1", new Vector3f(16, 258, 16));

        this.mat_terrain.setTexture("slopeColorMap", dirt);
        this.mat_terrain.setFloat("slopeTileFactor", 32);

        this.mat_terrain.setFloat("terrainSize", 512);

        this.base = new FractalSum();
        this.base.setRoughness(0.7f);
        this.base.setFrequency(1.0f);
        this.base.setAmplitude(1.0f);
        this.base.setLacunarity(2.12f);
        this.base.setOctaves(8);
        this.base.setScale(0.02125f);
        this.base.addModulator(new NoiseModulator() {
            @Override
            public float value(float... in) {
                return ShaderUtils.clamp(in[0] * 0.5f + 0.5f, 0, 1);
            }
        });

        FilteredBasis ground = new FilteredBasis(this.base);

        this.perturb = new PerturbFilter();
        this.perturb.setMagnitude(0.119f);

        this.therm = new OptimizedErode();
        this.therm.setRadius(5);
        this.therm.setTalus(0.011f);

        this.smooth = new SmoothFilter();
        this.smooth.setRadius(1);
        this.smooth.setEffect(0.7f);

        this.iterate = new IterativeFilter();
        this.iterate.addPreFilter(this.perturb);
        this.iterate.addPostFilter(this.smooth);
        this.iterate.setFilter(this.therm);
        this.iterate.setIterations(1);

        ground.addPreFilter(this.iterate);

        this.terrain = new TerrainGrid("terrain", 65, 1025, new FractalTileLoader(ground, 256f));

        this.terrain.setMaterial(this.mat_terrain);
        this.terrain.setLocalTranslation(0, 0, 0);
        this.terrain.setLocalScale(2f, 1f, 2f);
        this.terrain.setShadowMode(RenderQueue.ShadowMode.Receive);
        this.rootNode.attachChild(this.terrain);

        TerrainLodControl control = new TerrainGridLodControl(this.terrain, this.getCamera());
        control.setLodCalculator(new DistanceLodCalculator(65, 2.7f));
        this.terrain.addControl(control);
    }

    private void createCharacter() {
        SphereCollisionShape sphere = new SphereCollisionShape(5f);
        player = new CharacterControl(sphere, 0.01f);
        player.setFallSpeed(15f);
        player.setGravity(30f);
        model = (Node) assetManager.loadModel("Models/HoverTank/Tank2.mesh.xml");
        model.addControl(player);
        model.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        player.warp(new Vector3f(0, 315f, 0));
        rootNode.attachChild(model);
    }

    private void initPhysics() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        bulletAppState.getPhysicsSpace().add(player);

        this.terrain.addControl(new RigidBodyControl(0));

        bulletAppState.getPhysicsSpace().add(player);
        terrain.addListener(new TerrainGridListener() {
            public void gridMoved(Vector3f newCenter) {
            }

            public void tileAttached(Vector3f cell, TerrainQuad quad) {
                while (quad.getControl(RigidBodyControl.class) != null) {
                    quad.removeControl(RigidBodyControl.class);
                }
                quad.addControl(new RigidBodyControl(new HeightfieldCollisionShape(quad.getHeightMap(), terrain.getLocalScale()), 0));
                bulletAppState.getPhysicsSpace().add(quad);
            }

            public void tileDetached(Vector3f cell, TerrainQuad quad) {
                if (quad.getControl(RigidBodyControl.class) != null) {
                    bulletAppState.getPhysicsSpace().remove(quad);
                    quad.removeControl(RigidBodyControl.class);
                }
            }
        });
    }

    private void setUpKeys() {
        inputManager.addMapping("Rotate Left", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Rotate Right", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("Walk Forward", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Walk Backward", new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this, "Rotate Left", "Rotate Right");
        inputManager.addListener(this, "Walk Forward", "Walk Backward");
        inputManager.addListener(this, "Jump");
    }

    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Rotate Left")) {
            if (value) {
                leftRotate = true;
            } else {
                leftRotate = false;
            }
        } else if (binding.equals("Rotate Right")) {
            if (value) {
                rightRotate = true;
            } else {
                rightRotate = false;
            }
        } else if (binding.equals("Walk Forward")) {
            if (value) {
                forward = true;
            } else {
                forward = false;
            }
        } else if (binding.equals("Walk Backward")) {
            if (value) {
                backward = true;
            } else {
                backward = false;
            }
        } else if (binding.equals("Jump")) {
            player.jump();
        }
    }

    private void initCam() {
        flyCam.setEnabled(false);
        camNode = new CameraNode("CamNode", cam);
        camNode.setControlDir(ControlDirection.SpatialToCamera);
        camNode.setLocalTranslation(new Vector3f(0, 10, -25));
        camNode.lookAt(model.getLocalTranslation(), Vector3f.UNIT_Y);
        model.attachChild(camNode);
    }

    private void processor() {
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-4.9236743f, -1.27054665f, 5.896916f));
        sun.setColor(ColorRGBA.White.clone().multLocal(1.7f));
        this.rootNode.addLight(sun);

        DirectionalLight l = new DirectionalLight();
        l.setDirection(Vector3f.UNIT_Y.mult(-1));
        l.setColor(ColorRGBA.White.clone().multLocal(0.3f));
        this.rootNode.addLight(l);

        AmbientLight ambient = new AmbientLight();
        ambient.setColor(new ColorRGBA(0.7f, 0.7f, 0.7f, 1.0f));
        rootNode.addLight(ambient);

        DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, 2048, 2);
        dlsr.setLight(sun);
        viewPort.addProcessor(dlsr);

        Spatial sky = SkyFactory.createSky(assetManager, "Scenes/Beach/FullskiesSunset0068.dds", false);
        sky.setLocalScale(350);
        this.rootNode.attachChild(sky);

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        int numSamples = getContext().getSettings().getSamples();
        if (numSamples > 0) {
            fpp.setNumSamples(numSamples);
        }
        FogFilter fog = new FogFilter();
        fog.setFogColor(new ColorRGBA(165f, 145f, 121f, 1.0f));
        fog.setFogDistance(2000);
        fog.setFogDensity(0.0425f);
        fpp.addFilter(fog);
        viewPort.addProcessor(fpp);
    }

    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}