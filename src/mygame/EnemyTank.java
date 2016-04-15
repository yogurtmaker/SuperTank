package mygame;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.shape.Box;
import java.util.Random;

public class EnemyTank extends Enemy {

    Random rn;
    String[] states = {"RotateLeft", "RotateRight", "WalkForward", "WalkBackward", "Shot", "Stop"};
    String binding = states[5];
    Vector3f bulletPosition, velocity, tankPostion, playerDirection, leftDirection,
            rightDirection, view = new Vector3f(0, 0, 0);
    public boolean walk = false, attached = false, detached = false, bulletCreated = false, track = false;
    float time = 0, time2 = 0, time3 = 0;
    final int ROTATETIME = 5, WALKTIME = 5, FORCETIME = 3, SHOOTTIME = 10, TRACKDISTANCE = 500, ATTACKDISTANCE = 200;
    float stateTime, frequency, resetTime;
    int state;

    public EnemyTank(Main main, Material mat) {
        super(main, "Models/HoverTank/Tank2.mesh.xml", mat);
        initStuff();
    }

    void initStuff() {
        //health bar
        Box box = new Box(2.5f, 0.3f, 0.3f);
        bar = new Geometry("bar", box);
        Material matBar = new Material(main.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        matBar.setColor("Color", new ColorRGBA(1f, 0f, 0f, 0.2f));
        matBar.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        bar.setMaterial(matBar);
        bar.setLocalTranslation(0, 3.3f, 2);
        bar.setQueueBucket(RenderQueue.Bucket.Transparent);
        enemyNode.attachChild(bar);

        rn = new Random();
        enemyNode.addControl(new tankControl());
        stateTime = rn.nextFloat() * 5 + 5;
        System.out.println(stateTime);
        frequency = rn.nextFloat() * 2 + 1;
        System.out.println(frequency);
        dust = new Dust(main);
        dust.emit.setParticlesPerSec(20f);
        enemyNode.attachChild(dust.emit);
    }

    protected void aimPlaer(Quaternion rotLeft, Quaternion rotRight, Quaternion limLeft, Quaternion limRight, Quaternion rotReset) {
        leftDirection = leftNode1.getWorldTranslation().subtract(tankPostion);
        rightDirection = rightNode1.getWorldTranslation().subtract(tankPostion);
        if (leftDirection.normalize().subtract(playerDirection.normalize()).length() < 0.04
                || rightDirection.normalize().subtract(playerDirection.normalize()).length() < 0.04) {
            if (enemyNode.getChild(0).getLocalRotation().getZ() > rotReset.getZ() && resetTime > 0) {
                resetTime--;
                enemyNode.getChild(0).rotate(rotLeft);
            } else if (enemyNode.getChild(0).getLocalRotation().getZ() < rotReset.getZ() && resetTime > 0) {
                resetTime--;
                enemyNode.getChild(0).rotate(rotRight);
            } else if (resetTime <= 0) {
                enemyNode.getChild(0).setLocalRotation(rotReset);
            }
        } else if (leftDirection.subtract(playerDirection).length() < rightDirection.subtract(playerDirection).length()) {
            resetTime = 90;
            enemyControl.setViewDirection(leftDirection);
            if (enemyNode.getChild(0).getLocalRotation().getZ() >= limLeft.getZ()) {
                enemyNode.getChild(0).rotate(rotLeft);
            }
        } else if (leftDirection.subtract(playerDirection).length() > rightDirection.subtract(playerDirection).length()) {
            resetTime = 100;
            enemyControl.setViewDirection(rightDirection);
            if (enemyNode.getChild(0).getLocalRotation().getZ() <= limRight.getZ()) {
                enemyNode.getChild(0).rotate(rotRight);
            }
        } else {
            if (enemyNode.getChild(0).getLocalRotation().getZ() > rotReset.getZ() && resetTime > 0) {
                resetTime--;
                enemyNode.getChild(0).rotate(rotLeft);
            } else if (enemyNode.getChild(0).getLocalRotation().getZ() < rotReset.getZ() && resetTime > 0) {
                resetTime--;
                enemyNode.getChild(0).rotate(rotRight);
            } else if (resetTime <= 0) {
                enemyNode.getChild(0).setLocalRotation(rotReset);
            }
        }
        enemyControl.setWalkDirection(velocity.mult(0.2f));
    }

    protected void move(Quaternion rotLeft, Quaternion rotRight, Quaternion limLeft, Quaternion limRight, Quaternion rotReset) {
        if (leftRotate) {
            resetTime = 90;
            leftDirection = leftNode.getWorldTranslation().subtract(tankPostion);
            enemyControl.setViewDirection(leftDirection);
            if (enemyNode.getChild(0).getLocalRotation().getZ() >= limLeft.getZ()) {
                enemyNode.getChild(0).rotate(rotLeft);
            }
        } else if (rightRotate) {
            resetTime = 100;
            rightDirection = rightNode.getWorldTranslation().subtract(tankPostion);
            enemyControl.setViewDirection(rightDirection);
            if (enemyNode.getChild(0).getLocalRotation().getZ() <= limRight.getZ()) {
                enemyNode.getChild(0).rotate(rotRight);
            }
        } else {
            if (enemyNode.getChild(0).getLocalRotation().getZ() > rotReset.getZ() && resetTime > 0) {
                resetTime--;
                enemyNode.getChild(0).rotate(rotLeft);
            } else if (enemyNode.getChild(0).getLocalRotation().getZ() < rotReset.getZ() && resetTime > 0) {
                resetTime--;
                enemyNode.getChild(0).rotate(rotRight);
            } else if (resetTime <= 0) {
                enemyNode.getChild(0).setLocalRotation(rotReset);
            }
        }
    }

    @Override
    protected void adjust(Vector3f playerPos) {
        enemyControl.warp(new Vector3f(playerPos.x + (float) Math.random() * 200 - 100, 350f, playerPos.z + (float) Math.random() * 200 - 100));
    }

    @Override
    protected void updateEnemy(float tpf, Vector3f playerPos) {
        Quaternion rotLeft = new Quaternion().fromAngles(0, 0, -FastMath.PI * tpf / 4);
        Quaternion rotRight = new Quaternion().fromAngles(0, 0, FastMath.PI * tpf / 4);
        Quaternion rotReset = new Quaternion().fromAngles(0, 0, 0);
        Quaternion limLeft = new Quaternion().fromAngles(0, 0, -FastMath.PI / 4);
        Quaternion limRight = new Quaternion().fromAngles(0, 0, FastMath.PI / 4);
        time += tpf;
        for (Bullet bullet : bulletList) {
            bullet.update(tpf);
        }
        tankPostion = enemyNode.getWorldTranslation();
        playerDirection = playerPos.subtract(tankPostion);
        if (playerDirection.length() > TRACKDISTANCE) {
            track = true;
        } else {
            track = false;
        }

        if (playerDirection.length() < ATTACKDISTANCE) {
            attack = true;
        } else {
            attack = false;
        }

        if (hitPoints <= 0) {
            death = true;
        } else {
            death = false;
        }
        bulletPosition = bulletStartNode.getWorldTranslation();
        velocity = bulletPosition.subtract(tankPostion)
                .subtract(new Vector3f(0, 3, 0)).mult(1.01f);
        if (!death) {
            if (time > 15) {
                if (collideWithPlayer || collideWithEnemy) {
                    enemyControl.setWalkDirection(velocity.mult(0.3f).negate());
                } else if (track) {
                    aimPlaer(rotLeft, rotRight, limLeft, limRight, rotReset);
                } else if (attack) {
                    leftDirection = leftNode1.getWorldTranslation().subtract(tankPostion);
                    rightDirection = rightNode1.getWorldTranslation().subtract(tankPostion);
                    if (leftDirection.normalize().subtract(playerDirection.normalize()).length() < 0.04
                            || rightDirection.normalize().subtract(playerDirection.normalize()).length() < 0.04) {
                        shoot = true;
                    } else {
                        shoot = false;
                    }
                    aimPlaer(rotLeft, rotRight, limLeft, limRight, rotReset);
                } else if (forward) {
                    dust.emit.setParticlesPerSec(20);
                    enemyControl.setWalkDirection(velocity.mult(0.1f));
                    move(rotLeft, rotRight, limLeft, limRight, rotReset);
                } else if (backward) {
                    enemyControl.setWalkDirection(velocity.mult(0.1f).negate());
                    move(rotLeft, rotRight, limLeft, limRight, rotReset);
                } else {
                    if (enemyNode.getChild(0).getLocalRotation().getZ() > rotReset.getZ() && resetTime > 0) {
                        resetTime--;
                        enemyNode.getChild(0).rotate(rotLeft);
                    } else if (enemyNode.getChild(0).getLocalRotation().getZ() < rotReset.getZ() && resetTime > 0) {
                        resetTime--;
                        enemyNode.getChild(0).rotate(rotRight);
                    } else if (resetTime <= 0) {
                        enemyNode.getChild(0).setLocalRotation(rotReset);
                    }
                }
                if (shoot) {
                    float passTime = time - time2;
                    if (passTime > frequency) {
                        if (!bulletCreated) {
                            Bullet bullet = new Bullet(main, bulletStartNode.getWorldTranslation(),
                                    enemyNode.getWorldTranslation());
                            bullet.bullet.setLocalRotation(enemyNode.getLocalRotation());
                            bulletList.add(bullet);
                            main.getRootNode().attachChild(bullet.bullet);
                            bulletCreated = true;
                            time2 = time;
                        }
                    } else {
                        bulletCreated = false;
                    }
                }
            }
        }
    }

    class tankControl extends AbstractControl {

        float rotateLeftTime = 0;
        float rotateRightTime = 0;
        float walkForwardTime = 0;
        float walkBackwardTime = 0;
        float forceTime = 0;
        float shootTime = 0;
        float time = 0;

        @Override
        protected void controlUpdate(float tpf) {
            time += tpf;
            if (time - time3 >= stateTime) {
                stateTime = rn.nextFloat() * 5 + 5;
                frequency = rn.nextFloat() * 2 + 1;
                state = rn.nextInt(states.length);
                binding = states[state];
                time3 = time;
            }
            if (binding.equals("RotateLeft") && !rightRotate) {
                leftRotate = true;
            } else if (binding.equals("RotateRight") && !leftRotate) {
                rightRotate = true;
            } else if (binding.equals("WalkForward") && !backward) {
                forward = true;
            } else if (binding.equals("WalkBackward") && !forward) {
                backward = true;
            } else if (binding.equals("Shot")) {
                shoot = true;
            }

            if (leftRotate) {
                rotateLeftTime += tpf;
                if (rotateLeftTime > ROTATETIME) {
                    rotateLeftTime = 0;
                    Quaternion quan = new Quaternion().fromAngles(0, 0, 0);
                    enemyNode.getChild(0).setLocalRotation(quan);
                    leftRotate = false;
                }
            }
            if (rightRotate) {
                rotateRightTime += tpf;
                if (rotateRightTime > ROTATETIME) {
                    rotateRightTime = 0;
                    Quaternion quan = new Quaternion().fromAngles(0, 0, 0);
                    enemyNode.getChild(0).setLocalRotation(quan);
                    rightRotate = false;
                }
            }
            if (forward) {
                walkForwardTime += tpf;
                if (walkForwardTime > WALKTIME) {
                    walkForwardTime = 0;
                    forward = false;
                }
            }
            if (backward) {
                walkBackwardTime += tpf;
                if (walkBackwardTime > WALKTIME) {
                    walkBackwardTime = 0;
                    backward = false;
                }
            }
            if (force) {
                forceTime += tpf;
                if (forceTime > FORCETIME) {
                    force = false;
                    forceTime = 0;
                }
            }
            if (shoot) {
                shootTime += tpf;
                if (shootTime > SHOOTTIME) {
                    shoot = false;
                    shootTime = 0;
                }
            }
        }

        @Override
        protected void controlRender(RenderManager rm, ViewPort vp) {
        }
    }
}
