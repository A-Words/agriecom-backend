package net.awords.agriecombackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.awords.agriecombackend.dto.ApiResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/connectivity")
@Tag(name = "Connectivity", description = "数据库与Redis连通性检查")
public class ConnectivityController {

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public ConnectivityController(JdbcTemplate jdbcTemplate, StringRedisTemplate redisTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
    }

    @Operation(summary = "检查 PostgreSQL 与 Redis 连通性")
    @GetMapping
    public ApiResponseDTO<Map<String, Object>> checkConnectivity() {
        Map<String, Object> result = new HashMap<>();

        // Check PostgreSQL
        try {
            Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            result.put("postgres", one != null && one == 1 ? "UP" : "UNKNOWN");
        } catch (Exception ex) {
            result.put("postgres", "DOWN: " + ex.getMessage());
        }

        // Check Redis
        try {
            String pong = redisTemplate.getRequiredConnectionFactory().getConnection().ping();
            result.put("redis", (pong != null && pong.equalsIgnoreCase("PONG")) ? "UP" : "UNKNOWN");
        } catch (Exception ex) {
            result.put("redis", "DOWN: " + ex.getMessage());
        }

        return ApiResponseDTO.success(result);
    }
}
