package org.techtown.boda;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LabelList {

    private static final List<String> labels;

    static {
        List<String> tempLabels = new ArrayList<>();
        tempLabels.add("person");
        tempLabels.add("bicycle");
        tempLabels.add("car");
        tempLabels.add("motorcycle");
        tempLabels.add("airplane");
        tempLabels.add("bus");
        tempLabels.add("train");
        tempLabels.add("truck");
        tempLabels.add("boat");
        tempLabels.add("traffic light");
        tempLabels.add("fire hydrant");
        tempLabels.add("stop sign");
        tempLabels.add("parking meter");
        tempLabels.add("bench");
        tempLabels.add("bird");
        tempLabels.add("cat");
        tempLabels.add("dog");
        tempLabels.add("horse");
        tempLabels.add("sheep");
        tempLabels.add("cow");
        tempLabels.add("elephant");
        tempLabels.add("bear");
        tempLabels.add("zebra");
        tempLabels.add("giraffe");
        tempLabels.add("backpack");
        tempLabels.add("umbrella");
        tempLabels.add("handbag");
        tempLabels.add("tie");
        tempLabels.add("suitcase");
        tempLabels.add("frisbee");
        tempLabels.add("skis");
        tempLabels.add("snowboard");
        tempLabels.add("sports ball");
        tempLabels.add("kite");
        tempLabels.add("baseball bat");
        tempLabels.add("baseball glove");
        tempLabels.add("skateboard");
        tempLabels.add("surfboard");
        tempLabels.add("tennis racket");
        tempLabels.add("bottle");
        tempLabels.add("wine glass");
        tempLabels.add("cup");
        tempLabels.add("fork");
        tempLabels.add("knife");
        tempLabels.add("spoon");
        tempLabels.add("bowl");
        tempLabels.add("banana");
        tempLabels.add("apple");
        tempLabels.add("sandwich");
        tempLabels.add("orange");
        tempLabels.add("broccoli");
        tempLabels.add("carrot");
        tempLabels.add("hot dog");
        tempLabels.add("pizza");
        tempLabels.add("donut");
        tempLabels.add("cake");
        tempLabels.add("chair");
        tempLabels.add("couch");
        tempLabels.add("potted plant");
        tempLabels.add("bed");
        tempLabels.add("dining table");
        tempLabels.add("toilet");
        tempLabels.add("tv");
        tempLabels.add("laptop");
        tempLabels.add("mouse");
        tempLabels.add("remote");
        tempLabels.add("keyboard");
        tempLabels.add("cell phone");
        tempLabels.add("microwave");
        tempLabels.add("oven");
        tempLabels.add("toaster");
        tempLabels.add("sink");
        tempLabels.add("refrigerator");
        tempLabels.add("book");
        tempLabels.add("clock");
        tempLabels.add("vase");
        tempLabels.add("scissors");
        tempLabels.add("teddy bear");
        tempLabels.add("hair drier");
        tempLabels.add("toothbrush");
        labels = Collections.unmodifiableList(tempLabels);
    }

    public static List<String> getLabels() {
        return labels;
    }

    public static boolean hasLabel(String label) {
        return labels.contains(label);
    }
}