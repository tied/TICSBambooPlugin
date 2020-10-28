package com.tiobe.plugins.bamboo.results;

import com.atlassian.bamboo.chains.ChainResultsSummary;
import com.atlassian.bamboo.chains.ChainStageResult;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class QualityGateResultContextProvider implements ContextProvider {
    private static final Logger logger = Logger.getLogger(QualityGateResultContextProvider.class);

    @Override
    public void init(Map<String, String> map) throws PluginParseException {

    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context) {

        String qualityGateResult = "";

        if (context.containsKey("resultSummary")) {
            ChainResultsSummary chainResultSummary = (ChainResultsSummary) context.get("resultSummary");
            List<ChainStageResult> stageResults = chainResultSummary.getStageResults();
            for (ChainStageResult stageResult : stageResults) {

                Set<BuildResultsSummary> buildResults = stageResult.getBuildResults();
                for (BuildResultsSummary summary : buildResults) {
                    if (summary.getCustomBuildData().containsKey("ticsQualityGateResult")) {
                        qualityGateResult = summary.getCustomBuildData().get("ticsQualityGateResult");

                        Gson gson = new Gson();
                        try {
                            TicsQualityGateResult result = gson.fromJson(qualityGateResult, TicsQualityGateResult.class);
                            result.setUrl(summary.getCustomBuildData().getOrDefault("viewerApi", "") + "/" + result.getUrl());
                            context.put("ticsQualityGateResult", result);
                            context.put("projectName", summary.getCustomBuildData().getOrDefault("projectName", ""));
                            context.put("branchName", summary.getCustomBuildData().getOrDefault("branchName", ""));
                        } catch (JsonSyntaxException e) {
                            throw new RuntimeException("Failed to parse server response: " + qualityGateResult, e);
                        }

                        logger.debug("TICSQualityGateResult: " + context.get("ticsQualityGateResult"));
                        break;
                    }
                }
            }
        }

        return context;
    }
}
