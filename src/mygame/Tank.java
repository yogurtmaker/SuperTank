package mygame;

import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapText;
import com.jme3.input.controls.ActionListener;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import java.util.ArrayList;
import java.util.List;

public class Tank implements ActionListener {

    Main main;
    Node tankNode, bulletStartNode, walkDirNode;
    CharacterControl tankControl;
    List<Bullet> bulletList;
    Shield shield;
    Dust dust;
    Geometry bar;
    Vector3f walkDirection = new Vector3f(0, 0, 0), viewDirection = new Vector3f(0, 0, 0);
    boolean force = false, second = false, forward = false, backward = false,
            leftRotate = false, rightRotate = false;
    private float airTime = 0;
    private int resetTime;
    public float hitPoints = 100;
    Vector3f shieldBarPos = new Vector3f(820, 700, 0);
    int numberOfBulletRemain = 100;

    public Tank(Main main) {
        this.main = main;
        initTank();
    }

    private void initTank() {

        bulletList = new ArrayList<Bullet>();
        SphereCollisionShape sphere = new SphereCollisionShape(5f);
        tankControl = new CharacterControl(sphere, 0.01f);
        tankControl.setFallSpeed(15f);
        tankControl.setGravity(30f);
        tankNode = (Node) main.getAssetManager().loadModel("Models/HoverTank/Tank2.mesh.xml");
        tankNode.addControl(tankControl);
        tankNode.addControl(new RigidBodyControl(0));
        tankNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        tankControl.warp(new Vector3f(0, 320f, 0));

        bulletStartNode = new Node();
        bulletStartNode.setLocalTranslation(0, 2, 3);
        tankNode.attachChild(bulletStartNode);

        shield = new Shield(main);
        dust = new Dust(main);
        dust.emit.setParticlesPerSec(0f);
        tankNode.attachChild(dust.emit);

        //health bar
        Box box = new Box(2.5f, 0.3f, 0.3f);
        bar = new Geometry("bar", box);
        Material matBar = new Material(main.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        matBar.setColor("Color", new ColorRGBA(1f, 0f, 0f, 0.2f));
        matBar.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        bar.setMaterial(matBar);
        bar.setLocalTranslation(0, 3.3f, 2);
        bar.setQueueBucket(RenderQueue.Bucket.Transparent);
        tankNode.attachChild(bar);

    }

    public void onAction(String binding, boolean isPressed, float tpf) {
        if (binding.equals("Rotate Left")) {
            if (isPressed) {
                resetTime = 90;
                leftRotate = true;
            } else {
                leftRotate = false;
            }
        } else if (binding.equals("Rotate Right")) {
            if (isPressed) {
                resetTime = 100;
                rightRotate = true;
            } else {
                rightRotate = false;
            }
        } else if (binding.equals("Walk Forward")) {
            if (isPressed) {
                forward = true;
                dust.emit.setParticlesPerSec(20);
            } else {
                forward = false;
                dust.emit.setParticlesPerSec(0);
            }
        } else if (binding.equals("Walk Backward")) {
            if (isPressed) {
                backward = true;
            } else {
                backward = false;
            }
        } else if (binding.equals("Shot") && isPressed) {
            if (numberOfBulletRemain > 0) {
                numberOfBulletRemain--;
                Bullet bullet = new Bullet(main, bulletStartNode.getWorldTranslation(),
                        tankNode.getWorldTranslation());
                bullet.bullet.setLocalRotation(tankNode.getLocalRotation());
                bulletList.add(bullet);
                main.getRootNode().attachChild(bullet.bullet);
            }
        } else if (binding.equals("Shield")) {
            if (isPressed) {
                if (!second) {
                    force = true;
                    second = true;
                } else {
                    force = false;
                    second = false;
                }
            }
        }
    }

    public void updateTank(float tpf, BitmapText text) {
        for (Bullet bullet : bulletList) {
            bullet.update(tpf);
        }
        Vector3f camDir = main.getCamera().getDirection().mult(0.2f);
        Vector3f camLeft = main.getCamera().getLeft().mult(0.2f);
        Quaternion rotLeft = new Quaternion().fromAngles(0, 0, -FastMath.PI * tpf / 4);
        Quaternion rotRight = new Quaternion().fromAngles(0, 0, FastMath.PI * tpf / 4);
        Quaternion resetRot = new Quaternion().fromAngles(0, 0, 0);
        Quaternion limLeft = new Quaternion().fromAngles(0, 0, -FastMath.PI / 4);
        Quaternion limRight = new Quaternion().fromAngles(0, 0, FastMath.PI / 4);
        camDir.y = 0;
        camLeft.y = 0;
        viewDirection.set(camDir);
        walkDirection.set(0, 0, 0);
        if (!tankControl.onGround()) {
            airTime = airTime + tpf;
        } else {
            airTime = 0;
        }
        if (forward) {
            walkDirection.addLocal(camDir.mult(5f));
            if (leftRotate) {
                viewDirection.addLocal(camLeft.mult(0.0075f));
                if (tankNode.getChild(0).getLocalRotation().getZ() >= limLeft.getZ()) {
                    tankNode.getChild(0).rotate(rotLeft);
                }
            } else if (rightRotate) {
                viewDirection.addLocal(camLeft.mult(0.0075f).negate());
                if (tankNode.getChild(0).getLocalRotation().getZ() <= limRight.getZ()) {
                    tankNode.getChild(0).rotate(rotRight);
                }
            } else {
                if (tankNode.getChild(0).getLocalRotation().getZ() > resetRot.getZ() && resetTime > 0) {
                    resetTime--;
                    tankNode.getChild(0).rotate(rotLeft);
                } else if (tankNode.getChild(0).getLocalRotation().getZ() < resetRot.getZ() && resetTime > 0) {
                    resetTime--;
                    tankNode.getChild(0).rotate(rotRight);
                } else if (resetTime <= 0) {
                    tankNode.getChild(0).setLocalRotation(resetRot);
                }
            }
        } else if (backward) {
            walkDirection.addLocal(camDir.mult(5f).negate());
            if (leftRotate) {
                viewDirection.addLocal(camLeft.mult(0.0075f).negate());
                if (tankNode.getChild(0).getLocalRotation().getZ() >= limLeft.getZ()) {
                    tankNode.getChild(0).rotate(rotLeft);
                }
            } else if (rightRotate) {
                viewDirection.addLocal(camLeft.mult(0.0075f));
                if (tankNode.getChild(0).getLocalRotation().getZ() <= limRight.getZ()) {
                    tankNode.getChild(0).rotate(rotRight);
                }
            } else {
                if (tankNode.getChild(0).getLocalRotation().getZ() > resetRot.getZ() && resetTime > 0) {
                    resetTime--;
                    tankNode.getChild(0).rotate(rotLeft);
                } else if (tankNode.getChild(0).getLocalRotation().getZ() < resetRot.getZ() && resetTime > 0) {
                    resetTime--;
                    tankNode.getChild(0).rotate(rotRight);
                } else if (resetTime <= 0) {
                    tankNode.getChild(0).setLocalRotation(resetRot);
                }
            }
        } else {
            if (tankNode.getChild(0).getLocalRotation().getZ() > resetRot.getZ() && resetTime > 0) {
                resetTime--;
                tankNode.getChild(0).rotate(rotLeft);
            } else if (tankNode.getChild(0).getLocalRotation().getZ() < resetRot.getZ() && resetTime > 0) {
                resetTime--;
                tankNode.getChild(0).rotate(rotRight);
            } else if (resetTime <= 0) {
                tankNode.getChild(0).setLocalRotation(resetRot);
            }
        }
        if (force && shield.hitPoints > 0) {
            text.setText("HP:" + (int) shield.hitPoints);
            text.setLocalTranslation(shieldBarPos);
            tankNode.attachChild(shield.nodeshield);
        } else if (!force) {
            tankNode.detachChild(shield.nodeshield);
            text.setLocalTranslation(0, 0, 0);
        }
        tankControl.setWalkDirection(walkDirection);
        tankControl.setViewDirection(viewDirection);
        if (airTime > 5f) {
            tankControl.setWalkDirection(Vector3f.ZERO);
        }
    }
}