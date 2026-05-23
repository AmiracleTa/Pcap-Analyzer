package com.hzcu.pcap.service;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TsharkCommandRunnerTests {

    @Test
    void packetCountCommandUsesMachineReadableOutput() {
        TsharkCommandRunner runner = new TsharkCommandRunner();

        assertThat(runner.packetCountCommand(Path.of("sample.pcapng")))
                .containsExactly("capinfos", "-M", "-c", "sample.pcapng");
    }

    @Test
    void parsePacketCountRejectsAbbreviatedHumanReadableCounts() {
        TsharkCommandRunner runner = new TsharkCommandRunner();

        assertThatThrownBy(() -> runner.parsePacketCount("Number of packets:   43 k"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("valid packet count");
    }

    @Test
    void parsePacketCountReadsMachineReadableCounts() {
        TsharkCommandRunner runner = new TsharkCommandRunner();

        assertThat(runner.parsePacketCount("Number of packets:   43174")).isEqualTo(43174L);
    }
}
