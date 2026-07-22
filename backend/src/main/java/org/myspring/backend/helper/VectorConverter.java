package org.myspring.backend.helper;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VectorConverter {


    public static String toPgVector(float[] vector) {

        return IntStream.range(0, vector.length)
                .mapToObj(i -> String.valueOf(vector[i]))
                .collect(Collectors.joining(
                        ",",
                        "[",
                        "]"
                ));

    }

}