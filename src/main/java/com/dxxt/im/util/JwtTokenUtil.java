package com.dxxt.im.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.dxxt.im.config.AppConfig;

public class JwtTokenUtil {

    public static String verifyToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(AppConfig.jwtSecret);
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getClaim("userId").asString();
    }

    public static String getJwtToken(String userId) {
        //创建加密算法
        Algorithm algorithm = Algorithm.HMAC256(AppConfig.jwtSecret);
        String token = JWT.create()
                //签发者
                .withIssuer("auth0")
                //自定义KV
                .withClaim("userId", userId)
                .sign(algorithm);
        return token;
    }
}

