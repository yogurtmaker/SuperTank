/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.shape.Sphere;
import jme3utilities.SimpleAppState;

/**
 *
 * @author 2
 */
public class Bullet {

    SimpleApplication sa;
    Geometry geom;
    boolean alive = true;
    float time = 0;
    Vector3f position;
    Vector3f velocity;
    Vector3f tankWorldTranslation;

    public Bullet(SimpleApplication sa, Vector3f bulletWorldTranslation, Vector3f tankWorldTranslation) {
        this.sa = sa;
        this.position = bulletWorldTranslation;
        this.tankWorldTranslation = tankWorldTranslation;
        velocity = position.subtract(tankWorldTranslation)
                .subtract(new Vector3f(0, 3.1f, 0)).mult(0.15f);
        initBullet();
    }

    private void initBullet() {
        Sphere b = new Sphere(100, 100, .5f);
        geom = new Geometry("Bullet", b);
        Material mat = new Material(sa.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
//       mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);
    }

    public void update(float tpf) {
        if (alive) {
            position = position.add(velocity);
            geom.setLocalTranslation(position);
        }
    }

    public Vector3f getPosition() {
        return geom.getLocalTranslation();
    }
}
