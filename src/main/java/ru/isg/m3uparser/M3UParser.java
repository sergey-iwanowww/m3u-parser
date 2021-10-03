package ru.isg.m3uparser;

import lombok.extern.slf4j.Slf4j;
import ru.isg.m3uparser.model.M3UEntry;
import ru.isg.m3uparser.model.M3UFile;
import ru.isg.m3uparser.model.M3UParsinProblem;
import ru.isg.m3uparser.model.M3UParsingResult;
import ru.isg.m3uparser.model.ProblemSeverity;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by s.ivanov on 03.10.2021.
 */
@Slf4j
public class M3UParser {
    public static M3UParsingResult parse(Path path, Charset charset) {

        if (path == null || charset == null) {
            throw new IllegalArgumentException();
        }

        M3UParsingResult.M3UParsingResultBuilder resultBuilder = M3UParsingResult.builder();

        List<M3UParsinProblem> problems = new ArrayList<>();

        boolean utf8ByFileName = path.getFileName().toString().endsWith(".m3u8");
        if (charset.equals(StandardCharsets.UTF_8) && !utf8ByFileName) {
            problems.add(new M3UParsinProblem(ProblemSeverity.WARNING, "File name encoding does not match " + charset));
        }

        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
            parse(reader, charset, resultBuilder, problems);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            problems.add(new M3UParsinProblem(ProblemSeverity.ERROR, "File reading error"));
        }

        return resultBuilder.problems(problems).build();
    }

    private static void parse(BufferedReader reader, Charset charset,
            M3UParsingResult.M3UParsingResultBuilder resultsBuilder,
            List<M3UParsinProblem> problems) throws IOException {

        M3UFile.M3UFileBuilder fileBuilder = M3UFile.builder();

        List<M3UEntry> entries = new ArrayList<>();

        int i = 0;

        M3UEntry.M3UEntryBuilder entryBuilder = M3UEntry.builder();
        Map<String, String> advanceDirectives = new HashMap<>();

        String str;
        while ((str = reader.readLine()) != null) {

            i++;

            str = str.trim();

            if (str.length() == 0) {
                continue;
            }

            if (!str.startsWith("#")) {
                entries.add(entryBuilder.path(str).build());
                entryBuilder = M3UEntry.builder();
                advanceDirectives = new HashMap<>();
            } else {

                if (str.length() == 1) {
                    problems.add(new M3UParsinProblem(ProblemSeverity.WARNING, "Empty directive on line " + i));
                    continue;
                }

                if (str.equals("#EXTM3U")) {
                    if (i == 1) {
                        fileBuilder.extm3u(true);
                    } else {
                        problems.add(
                                new M3UParsinProblem(ProblemSeverity.WARNING, "Directive EXTM3U not on first line"));
                    }
                } else if (str.startsWith("#EXTENC:")) {
                    if (i == 2) {
                        String enc = extractDirectiveStringValue(str);
                        fileBuilder.extEnc(enc);

                        if (!stripDirectiveValue(charset.name()).equalsIgnoreCase(enc)) {
                            problems.add(new M3UParsinProblem(ProblemSeverity.WARNING,
                                    "File encoding does not match " + charset + " on line " + i));
                        }
                    } else {
                        problems.add(
                                new M3UParsinProblem(ProblemSeverity.WARNING, "Directive EXTENC not on second line"));
                    }
                } else if (str.startsWith("#PLAYLIST:")) {
                    entryBuilder.playlist(extractDirectiveStringValue(str));
                } else if (str.startsWith("#EXTALB:")) {
                    entryBuilder.extAlb(extractDirectiveStringValue(str));
                } else if (str.startsWith("#EXTBIN:")) {
                    //entryBuilder.extBin(extractDirectiveValue(str)); // TODO: parse byte[]
                } else if (str.startsWith("#EXTBYT:")) {
                    entryBuilder.extByt(extractDirectiveLongValue(str));
                } else if (str.startsWith("#EXTART:")) {
                    entryBuilder.extArt(extractDirectiveStringValue(str));
                } else if (str.startsWith("#EXTGENRE:")) {
                    entryBuilder.extGenre(extractDirectiveStringValue(str));
                } else if (str.startsWith("#EXTGRP:")) {
                    entryBuilder.extGrp(extractDirectiveStringValue(str));
                } else if (str.startsWith("#EXTINF:")) {
                    entryBuilder.extInf(extractDirectiveStringValue(str));
                } else if (str.startsWith("#EXTM3A:")) {
                    entryBuilder.extM3A(extractDirectiveStringValue(str));
                } else if (str.startsWith("#EXTIMG:")) {
                    entryBuilder.extImg(extractDirectiveStringValue(str));
                } else if (str.startsWith("#EXT-X-VERSION:")) {
                    fileBuilder.extXVersion(extractDirectiveStringValue(str));
                } else if (str.startsWith("#EXT-X-PROGRAM-DATE-TIME:")) {
                    entryBuilder.extXProgramDateTime(extractDirectiveDateTimeValue(str));
                } else if (str.startsWith("#EXT-X-START:")) {
                    entryBuilder.extXStart(extractDirectiveStringValue(str));
                } else if (str.startsWith("#EXT-X-BYTERANGE:")) {
                    entryBuilder.extXByteRange(extractDirectiveStringValue(str));
                } else if (str.startsWith("#EXT-X-DATERANGE:")) {
                    entryBuilder.extXDateRange(extractDirectiveStringValue(str));
                } else if (str.equals("#EXT-X-DISCONTINUITY")) {
                    entryBuilder.extXDiscontinuity(true);
                } else if (str.startsWith("#EXT-X-DISCONTINUITY-SEQUENCE:")) {
                    entryBuilder.extXDiscountinuitySequence(extractDirectiveLongValue(str));
                } else if (str.equals("#EXT-X-ENDLIST")) {
                    entryBuilder.extXEndList(true);
                } else if (str.equals("#EXT-X-I-FRAMES-ONLY")) {
                    entryBuilder.extXIFramesOnly(true);
                } else if (str.startsWith("#EXT-X-I-FRAME-STREAM-INF:")) {
                    entryBuilder.extXIFrameStreamInf(extractDirectiveStringValue(str));
                } else if (str.equals("#EXT-X-INDEPENDENT-SEGMENTS:")) {
                    entryBuilder.extXIndependentSegments(true);
                } else if (str.startsWith("#EXT-X-KEY:")) {
                    entryBuilder.extXKey(extractDirectiveStringValue(str));
                } else if (str.startsWith("#EXT-X-MAP:")) {
                    entryBuilder.extXMap(extractDirectiveStringValue(str));
                } else if (str.startsWith("#EXT-X-MEDIA:")) {
                    entryBuilder.extXMedia(extractDirectiveStringValue(str));
                } else if (str.startsWith("#EXT-X-MEDIA-SEQUENCE:")) {
                    entryBuilder.extXMediaSequence(extractDirectiveLongValue(str));
                } else if (str.startsWith("#EXT-X-PLAYLIST-TYPE:")) {
                    entryBuilder.extXPlaylistType(extractDirectiveStringValue(str));
                } else if (str.startsWith("#EXT-X-SESSION-DATA:")) {
                    entryBuilder.extXSessionData(extractDirectiveStringValue(str));
                } else if (str.startsWith("#EXT-X-SESSION-KEY:")) {
                    entryBuilder.extXSessionKey(extractDirectiveStringValue(str));
                } else if (str.startsWith("#EXT-X-STREAM-INF:")) {
                    entryBuilder.extXStreamInf(extractDirectiveStringValue(str));
                } else if (str.startsWith("#EXT-X-TARGETDURATION:")) {
                    entryBuilder.extXTargetDuration(extractDirectiveLongValue(str));
                } else {
                    advanceDirectives.put(extractDirectiveName(str), extractDirectiveStringValue(str));
                }
            }
        }

        resultsBuilder.file(fileBuilder.entries(entries).build());
    }

    private static String extractDirectiveName(String str) {
        int beg = str.indexOf("#") + 1;
        if (str.contains(":")) {
            return str.substring(beg, str.indexOf(":")).trim();
        } else {
            return str.substring(beg).trim();
        }
    }

    private static String extractDirectiveStringValue(String str) {
        return str.substring(str.indexOf(":") + 1).trim();
    }

    private static OffsetDateTime extractDirectiveDateTimeValue(String str) {
        return OffsetDateTime.parse(extractDirectiveStringValue(str));
    }

    private static Long extractDirectiveLongValue(String str) {
        return Long.parseLong(extractDirectiveStringValue(str));
    }

    private static String stripDirectiveValue(String str) {
        return str.replaceAll("[^A-Za-z0-9]", "");
    }

    public static void main(String[] args) {
        M3UParsingResult res = parse(Paths.get("e:\\Downloads\\iptvchannels.m3u8"), StandardCharsets.UTF_8);
        log.info("res = " + res);
    }
}
