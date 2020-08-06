package com.dreamsecurity.shopface.service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.dreamsecurity.shopface.domain.Branch;
import com.dreamsecurity.shopface.domain.Member;
import com.dreamsecurity.shopface.dto.branch.BranchAddRequestDto;
import com.dreamsecurity.shopface.dto.branch.BranchEditRequestDto;
import com.dreamsecurity.shopface.dto.branch.BranchListResponseDto;
import com.dreamsecurity.shopface.dto.branch.BranchResponseDto;
import com.dreamsecurity.shopface.repository.BranchRepository;
import com.dreamsecurity.shopface.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class BranchServiceImpl implements BranchService {
    private final BranchRepository branchRepository;
    private final MemberRepository memberRepository;
    private AmazonS3 amazonS3;

    @PostConstruct
    public void setUp() {
        amazonS3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.AP_NORTHEAST_2).build();
    }

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Transactional
    @Override
    public Long addBranch(BranchAddRequestDto requestDto) {
        Member member = memberRepository.findById(requestDto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다"));

        requestDto.setMember(member);

        return branchRepository.save(requestDto.toEntity()).getNo();
    }

    @Transactional(readOnly = true)
    @Override
    public List<BranchListResponseDto> getBranchList(String memberId) {
        return branchRepository.findAllByMemberId(memberId);
    }

    @Transactional(readOnly = true)
    @Override
    public BranchResponseDto getBranch(long no) {
        Branch entity = branchRepository.findById(no)
                .orElseThrow(() -> new IllegalArgumentException("해당 지점이 없습니다."));
        return new BranchResponseDto(entity);
    }

    @Transactional
    @Override
    public Long editBranch(long no, BranchEditRequestDto requestDto)
            throws IOException {
        Branch entity = branchRepository.findById(no)
                .orElseThrow(() -> new IllegalArgumentException("해당 지점이 없습니다."));

        String businessLicensePath = "";
        if (requestDto.getBusinessLicenseImage() != null) {
            amazonS3.putObject(new PutObjectRequest(
                    this.bucket, requestDto.getBusinessLicenseImage().getOriginalFilename(),
                    requestDto.getBusinessLicenseImage().getInputStream(), null));

            businessLicensePath = amazonS3.getUrl(
                    this.bucket, requestDto.getBusinessLicenseImage().getOriginalFilename()).toString();
        }

        entity.update(requestDto.getName(), requestDto.getAddress(),
                requestDto.getDetailAddress(), requestDto.getZipCode(), businessLicensePath);

        return no;
    }

    @Transactional
    @Override
    public void removeBranch(long no) {
        Branch entity = branchRepository.findById(no)
                .orElseThrow(() -> new IllegalArgumentException("해당 지점이 없습니다."));

        branchRepository.delete(entity);
    }
}