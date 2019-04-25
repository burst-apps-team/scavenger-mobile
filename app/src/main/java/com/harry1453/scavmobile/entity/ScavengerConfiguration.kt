package com.harry1453.scavmobile.entity

import org.yaml.snakeyaml.Yaml
import java.io.Reader
import java.math.BigInteger
import kotlin.collections.HashMap

// Account IDs are BigInts to work around JVM's lack of unsigned types
class ScavengerConfiguration(
    var account_id_to_secret_phrase: MutableMap<BigInteger, String> = HashMap(),

    var plot_dirs: MutableList<String> = ArrayList(),

    var url: String = "http://dummypool.megash.it/",

    var hdd_reader_thread_count: Int = 0,
    var hdd_use_direct_io: Boolean = true,
    var hdd_wakeup_after: Int = 240,

    var cpu_threads: Int = 0,
    var cpu_worker_task_count: Int = 4,
    var cpu_nonces_per_cache: Int = 65536,
    var cpu_thread_pinning: Boolean = false,

    var gpu_threads: Int = 0,
    var gpu_platform: Int = 0,
    var gpu_device: Int = 0,
    var gpu_worker_task_count: Int = 0,
    var gpu_nonces_per_cache: Int = 262144,
    var gpu_mem_mapping: Boolean = false,
    var gpu_async: Boolean = false,

    var target_deadline: BigInteger = BigInteger("18446744073709551615"),
    var account_id_to_target_deadline: MutableMap<BigInteger, BigInteger> = HashMap(),

    var get_mining_info_interval: Int = 3000,
    var timeout: Int = 5000,
    var send_proxy_details: Boolean = true,

    var console_log_level: String = "info",
    var logfile_log_level: String = "warn",
    var logfile_max_count: Int = 10,
    var logfile_max_size: Int = 20,

    var show_progress: Boolean = true,
    var show_drive_stats: Boolean = false,
    var benchmark_only: String = "disabled",

    var console_log_pattern: String = "{({d(%H:%M:%S)} [{l}]):16.16} {m}{n}",
    var logfile_log_pattern: String = "{({d(%Y-%m-%d %H:%M:%S)} [{l}]):26.26} {m}{n}"
) {
    fun toYaml() = Yaml().dump(this)
    companion object {
        fun fromYaml(yaml: Reader) = Yaml().loadAs(yaml, ScavengerConfiguration::class.java)
    }
}
