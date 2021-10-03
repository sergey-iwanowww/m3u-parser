package ru.isg.m3uparser.model;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * Created by s.ivanov on 03.10.2021.
 */
@Data
@Builder
public class M3UEntry {

    // base m3u fields
    private String path;

    // ext m3u fields
    private String playlist;
    private String extInf;
    private String extGrp;
    private String extAlb;
    private String extArt;
    private String extGenre;
    private String extM3A;
    private Long extByt;
    private Byte[] extBin;
    private String extImg;

    // hls m3u fields
    private String extXByteRange;
    private boolean extXDiscontinuity;
    private String extXKey;
    private String extXMap;
    private OffsetDateTime extXProgramDateTime;
    private String extXDateRange;
    private Long extXTargetDuration;
    private Long extXMediaSequence;
    private Long extXDiscountinuitySequence;
    private boolean extXEndList;
    private String extXPlaylistType;
    private boolean extXIFramesOnly;
    private String extXMedia;
    private String extXStreamInf;
    private String extXIFrameStreamInf;
    private String extXSessionData;
    private String extXSessionKey;
    private boolean extXIndependentSegments;
    private String extXStart;

    // parsed referenced file (m3u only)
    private M3UFile file;
}
