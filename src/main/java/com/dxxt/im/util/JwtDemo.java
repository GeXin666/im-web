package com.dxxt.im.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

public class JwtDemo {

    public static void main(String[] main) {
        String token = null;
        try {
            //创建加密算法
            Algorithm algorithm = Algorithm.HMAC256("secret");
            token = JWT.create()
                    //签发者
                    .withIssuer("auth0")
                    //自定义KV
                    .withClaim("userId", "A02")
                    .sign(algorithm);
            System.out.println(token);
        } catch (JWTCreationException exception){
            //Invalid Signing configuration / Couldn't convert Claims.
        }

        //String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJhdXRoMCIsInVzZXJJZCI6IjExMTExMTExMDAwMDAwIn0.1PV7z-bX1JN_cqUospbmQ8IWjvsqQqEn9DaOVrrTfik";
        try {
            //创建加密算法
            Algorithm algorithm = Algorithm.HMAC256("secret");
            JWTVerifier verifier = JWT.require(algorithm).build();

            DecodedJWT jwt = verifier.verify(token);
            System.out.println(jwt.getClaim("userId").asString());
        } catch (JWTVerificationException exception){
            //Invalid signature/claims
            exception.printStackTrace();
        }
    }
}
