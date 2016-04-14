package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.util.TangentBinormalGenerator;

public class TestTankDissolve extends SimpleApplication {

    Spatial tank;
    private boolean dissolve = false;
    private final float speed = .25f;
    private float count = 0, time = 0;
    private int dir = 1;
    private Vector2f DSParams, DSParamsInv, DSParamsBurn;

    public static void main(final String[] args) {
        final TestTankDissolve app = new TestTankDissolve();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        this.assetManager.registerLocator("assets", FileLocator.class);

        this.cam.setLocation(new Vector3f(0, 1.5f, 10f));

        this.DSParams = new Vector2f(0, 0);
        this.DSParamsInv = new Vector2f(0, 1);
        this.DSParamsBurn = new Vector2f(0, 0);

        tank = assetManager.loadModel("Models/HoverTank/Tank2.mesh.xml");
       final  Material mat = this.assetManager
                .loadMaterial("Materials/Active/MultiplyColor_1.j3m");
        tank.setMaterial(mat);

        tank.setLocalTranslation(new Vector3f(0, 0, 0));
        TangentBinormalGenerator.generate(tank);
        this.rootNode.attachChild(tank);

        final AmbientLight a = new AmbientLight();
        a.setColor(ColorRGBA.White);
        this.rootNode.addLight(a);

        this.viewPort.setBackgroundColor(ColorRGBA.Gray);
        this.flyCam.setMoveSpeed(5);

    }

    @Override
    public void simpleUpdate(final float tpf) {
        time += tpf;
        if (time > 7.5 || dissolve == true) {
            final Material demat = this.assetManager
                    .loadMaterial("Materials/Deactive/MultiplyColor_Base.j3m");
            demat.setTexture("DissolveMap", this.assetManager.loadTexture("Textures/burnMap.png"));
            this.DSParams.setX(0);
            this.DSParamsInv.setX(0);
            this.DSParamsBurn.setX(0);
            demat.setVector2("DissolveParams", this.DSParams);
            tank.setMaterial(demat);
            dissolve = true;
            this.count += tpf * this.speed * this.dir;
        }
        if (this.count > 1f) {
            this.dir = 1;
        }
        this.DSParams.setX(this.count);
        this.DSParamsInv.setX(this.count);
        this.DSParamsBurn.setX(this.count - 0.05f);
    }
}
