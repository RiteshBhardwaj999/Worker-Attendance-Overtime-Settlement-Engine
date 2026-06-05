package com.taskmanager.controller;

import com.taskmanager.dto.request.InviteMemberRequest;
import com.taskmanager.dto.request.UpdateRoleRequest;
import com.taskmanager.dto.response.MemberResponse;
import com.taskmanager.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<List<MemberResponse>> list(@AuthenticationPrincipal UserDetails ud,
                                                      @PathVariable UUID projectId) {
        UUID userId = UUID.fromString(ud.getUsername());
        return ResponseEntity.ok(memberService.listMembers(userId, projectId));
    }

    @PostMapping
    public ResponseEntity<MemberResponse> invite(@AuthenticationPrincipal UserDetails ud,
                                                  @PathVariable UUID projectId,
                                                  @Valid @RequestBody InviteMemberRequest req) {
        UUID userId = UUID.fromString(ud.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.inviteMember(userId, projectId, req));
    }

    @PatchMapping("/{targetUserId}")
    public ResponseEntity<MemberResponse> changeRole(@AuthenticationPrincipal UserDetails ud,
                                                      @PathVariable UUID projectId,
                                                      @PathVariable UUID targetUserId,
                                                      @Valid @RequestBody UpdateRoleRequest req) {
        UUID userId = UUID.fromString(ud.getUsername());
        return ResponseEntity.ok(memberService.changeRole(userId, projectId, targetUserId, req));
    }

    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal UserDetails ud,
                                        @PathVariable UUID projectId,
                                        @PathVariable UUID targetUserId) {
        UUID userId = UUID.fromString(ud.getUsername());
        memberService.removeMember(userId, projectId, targetUserId);
        return ResponseEntity.noContent().build();
    }
}
