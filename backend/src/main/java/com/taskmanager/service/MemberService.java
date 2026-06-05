package com.taskmanager.service;

import com.taskmanager.dto.request.InviteMemberRequest;
import com.taskmanager.dto.request.UpdateRoleRequest;
import com.taskmanager.dto.response.MemberResponse;
import com.taskmanager.entity.Project;
import com.taskmanager.entity.ProjectMember;
import com.taskmanager.entity.User;
import com.taskmanager.exception.ConflictException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.repository.ProjectMemberRepository;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final ProjectMemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectService projectService;

    public List<MemberResponse> listMembers(UUID callerId, UUID projectId) {
        projectService.requireMember(callerId, projectId);
        return memberRepository.findAllByProject_Id(projectId)
                .stream().map(MemberResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public MemberResponse inviteMember(UUID callerId, UUID projectId, InviteMemberRequest req) {
        projectService.requireAdmin(callerId, projectId);
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No user found with email: " + req.getEmail()));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (memberRepository.existsByUserAndProject(user, project)) {
            throw new ConflictException("User is already a member of this project");
        }

        ProjectMember member = ProjectMember.builder()
                .user(user)
                .project(project)
                .role(req.getRole())
                .build();
        return MemberResponse.from(memberRepository.save(member));
    }

    @Transactional
    public MemberResponse changeRole(UUID callerId, UUID projectId, UUID targetUserId, UpdateRoleRequest req) {
        projectService.requireAdmin(callerId, projectId);
        ProjectMember member = memberRepository.findByUser_IdAndProject_Id(targetUserId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        member.setRole(req.getRole());
        return MemberResponse.from(memberRepository.save(member));
    }

    @Transactional
    public void removeMember(UUID callerId, UUID projectId, UUID targetUserId) {
        projectService.requireAdmin(callerId, projectId);
        ProjectMember member = memberRepository.findByUser_IdAndProject_Id(targetUserId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        memberRepository.delete(member);
    }
}
