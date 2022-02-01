package com.ssafy.pettodoctor.api.controller;

import com.ssafy.pettodoctor.api.domain.Doctor;
import com.ssafy.pettodoctor.api.domain.Hospital;
import com.ssafy.pettodoctor.api.service.DoctorService;
import com.ssafy.pettodoctor.api.service.HospitalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hospital")
@Tag(name = "hospital controller", description = "병원 관련 컨트롤러")
@CrossOrigin("*")
public class HospitalController {
    private final HospitalService hospitalService;

    @GetMapping("/{hospitalId}")
    @Operation(summary = "병원키에 해당하는 병원 정보 반환", description = "병원키에 해당하는 병원 정보를 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "사용자 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Map<String, Object>> findById(
            @PathVariable @Parameter(description = "병원키") Long hospitalId ) {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = null;

        try{
            Hospital hospital = hospitalService.findById(hospitalId);
            resultMap.put("hospital", hospital);
            resultMap.put("message", "성공");
            status = HttpStatus.OK;
        } catch (Exception e){
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            resultMap.put("message", "서버 오류");
        }

        return new ResponseEntity<Map<String, Object>>(resultMap, status);
    }

    @GetMapping("/dong/{dongCode}")
    @Operation(summary = "동코드에 해당하는 병원 정보 반환", description = "동코드에 해당하는 병원 정보 리스트를 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "사용자 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Map<String, Object>> findByDongCode(
            @PathVariable @Parameter(description = "동코드") String dongCode ) {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = null;

        try{
            List<Hospital> hospitals = hospitalService.findByDongCode(dongCode);
            resultMap.put("hospitals", hospitals);
            resultMap.put("message", "성공");
            status = HttpStatus.OK;
        } catch (Exception e){
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            resultMap.put("message", "서버 오류");
        }

        return new ResponseEntity<Map<String, Object>>(resultMap, status);
    }

    @GetMapping("/name")
    @Operation(summary = "이름에 해당하는 병원 정보 반환", description = "입력과 유사한 이름의 병원 정보 리스트를 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "사용자 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Map<String, Object>> findByName(
            @RequestParam @Parameter(description = "이름") String name ) {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = null;

        try{
            List<Hospital> hospitals = hospitalService.findByName(name);
            resultMap.put("hospitals", hospitals);
            resultMap.put("message", "성공");
            status = HttpStatus.OK;
        } catch (Exception e){
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            resultMap.put("message", "서버 오류");
        }

        return new ResponseEntity<Map<String, Object>>(resultMap, status);
    }
}
