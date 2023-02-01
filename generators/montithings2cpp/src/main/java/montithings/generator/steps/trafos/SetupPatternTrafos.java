package montithings.generator.steps.trafos;

import montithings.generator.MontiThingsGeneratorTool;
import montithings.generator.config.ApplyPatterns;
import montithings.generator.config.ConfigParams;
import montithings.generator.data.GeneratorToolState;
import montithings.generator.steps.GeneratorStep;
import montithings.generator.steps.trafos.patterns.AnomalyDetectionPatternTrafo;

public class SetupPatternTrafos extends GeneratorStep {
    private static final int WINDOW_SIZE = 5;
    private static final double TOLERANCE = 5.0;

    @Override
    public void action(GeneratorToolState state) {
        ConfigParams config = state.getConfig();
        MontiThingsGeneratorTool tool = state.getTool();

        if (config.getApplyPatterns() == ApplyPatterns.ON) {
            tool.addTrafo(new AnomalyDetectionPatternTrafo(state, WINDOW_SIZE, TOLERANCE));
        }
    }
}
