package ru.isg.m3uparser.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Created by s.ivanov on 03.10.2021.
 */
@Data
@Builder
public class M3UFile {
    private boolean extm3u;
    private String extEnc;
    private String extXVersion;
    private List<M3UEntry> entries;
}
