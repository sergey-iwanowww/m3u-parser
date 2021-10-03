package ru.isg.m3uparser.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by s.ivanov on 03.10.2021.
 */
@Data
@AllArgsConstructor
public class M3UParsinProblem {
    private ProblemSeverity severity;
    private String message;
}
