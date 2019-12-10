package com.sunxy.plugin;

import com.android.build.gradle.BaseExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

/**
 * plugin
 */
public class JavaSunxyPlugin implements Plugin<Project> {

    private String buildType = "";

    @Override
    public void apply(@NotNull Project project) {
        SunxyExtension config = project.getExtensions().create("SunxyPluginConfig", SunxyExtension.class);
        buildType = getBuildType(project);

        BaseExtension baseExtension = project.getExtensions().findByType(BaseExtension.class);
        JavaSunxyTransform transform = new JavaSunxyTransform(project, baseExtension, config, buildType);
        baseExtension.registerTransform(transform);

    }


    /**
     * 获取当前buildType
     */
    private String getBuildType( Project project){
        String buildConfigStr = project.getGradle().getStartParameter().getTaskRequests().toString();
        if (buildConfigStr.contains("Debug")) {
            return "Debug";
        } else if (buildConfigStr.contains("Release")) {
            return"Release";
        }
        return "";
    }
}
