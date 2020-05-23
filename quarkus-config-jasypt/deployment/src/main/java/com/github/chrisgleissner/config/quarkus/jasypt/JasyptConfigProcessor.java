package com.github.chrisgleissner.config.quarkus.jasypt;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class JasyptConfigProcessor {

    @BuildStep
    public FeatureBuildItem feature() {
        return new FeatureBuildItem("config-jasypt");
    }
}
