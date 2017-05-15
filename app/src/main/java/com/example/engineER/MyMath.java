package com.example.engineER;

public class MyMath {

    public static vec3 cross(vec3 x, vec3 y) {
        return
                new vec3(x.y * y.z - y.y * x.z,
                x.z * y.x - y.z * x.x,
                x.x * y.y - y.x * x.y);
    }

    public static float inversesqrt(float sqr) {
        return (float)(1.0f/Math.sqrt(sqr));
    }
    public static vec3 normalize(vec3 x) {

        float sqr = x.x * x.x + x.y * x.y + x.z * x.z;

        return new vec3(x.x * MyMath.inversesqrt(sqr), x.y * MyMath.inversesqrt(sqr), x.z * MyMath.inversesqrt(sqr));
    }
}

class vec3 {
    float x,y,z;

    public vec3(float x,float y, float z) {
        this.x=x;
        this.y=y;
        this.z=z;
    }

    public vec3 minus(vec3 a) {
        this.x-=a.x;
        this.y-=a.y;
        this.z-=a.z;

        return this;
    }

    public vec3 plus(vec3 a) {
        this.x+=a.x;
        this.y+=a.y;
        this.z+=a.z;

        return this;
    }

    public vec3 multiplyScalar(float scalar) {
        this.x*=scalar;
        this.y*=scalar;
        this.z*=scalar;

        return this;
    }


}
