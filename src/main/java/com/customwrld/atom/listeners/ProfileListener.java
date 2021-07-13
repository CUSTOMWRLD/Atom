package com.customwrld.atom.listeners;

import com.customwrld.atom.Atom;
import com.customwrld.commonlib.modules.pigeon.payloads.profile.ProfileCreatePayload;
import com.customwrld.commonlib.modules.pigeon.payloads.profile.ProfileRequestPayload;
import com.customwrld.commonlib.modules.pigeon.payloads.profile.ProfileUpdatePayload;
import com.customwrld.commonlib.modules.profile.Profile;
import com.customwrld.commonlib.modules.profile.ProfileQueryResult;
import com.customwrld.pigeon.annotations.PayloadHandler;
import com.customwrld.pigeon.annotations.PayloadListener;
import com.customwrld.pigeon.feedback.RequiredState;
import com.customwrld.pigeon.listeners.Listener;

@PayloadListener
public class ProfileListener implements Listener {

    @PayloadHandler(requiredState = RequiredState.REQUEST)
    public void onProfileCreate(ProfileCreatePayload payload) {
        final var profilesManager = Atom.getAtom().getProfilesManager();

        var profile = payload.getProfile();
        var id = profile.getId();

        profilesManager
                .getProfile(id)
                .thenAccept(existingProfile -> {
                    profilesManager
                            .createProfile(profile)
                            .thenAccept(result -> payload.sendResponse(new ProfileCreatePayload(result)));
                });
    }

    @PayloadHandler(requiredState = RequiredState.REQUEST)
    public void onProfileRequest(final ProfileRequestPayload payload) {
        final var profilesManager = Atom.getAtom().getProfilesManager();

        var name = payload.getName();
        var id = payload.getId();

        if(id != null && name != null) {
            var profile = new Profile(name, id);
            profilesManager
                    .getProfile(id)
                    .thenAccept(existingProfile -> {
                        if(existingProfile != null) {
                            payload.sendResponse(new ProfileRequestPayload(ProfileQueryResult.RETRIEVED, existingProfile));
                            return;
                        }

                        profilesManager
                                .createProfile(profile)
                                .thenAccept(result -> payload.sendResponse(new ProfileRequestPayload(result, profile)));
                    });

            return;
        }

        if(id != null) {
            profilesManager
                    .getProfile(id)
                    .thenAccept(profile -> {
                        payload.sendResponse(new ProfileRequestPayload(
                                (profile != null) ?
                                        ProfileQueryResult.RETRIEVED :
                                        ProfileQueryResult.NOT_FOUND,
                                profile));
                    });

            return;
        }

        if(name != null) {
            profilesManager
                    .getProfile(name)
                    .thenAccept(profile -> {
                        payload.sendResponse(new ProfileRequestPayload(
                                (profile != null) ?
                                        ProfileQueryResult.RETRIEVED :
                                        ProfileQueryResult.NOT_FOUND,
                                profile));
                    });
        }
    }

    @PayloadHandler(requiredState = RequiredState.REQUEST)
    public void onProfileUpdate(ProfileUpdatePayload payload) {
        final var profilesManager = Atom.getAtom().getProfilesManager();

        var profile = payload.getProfile();
        var id = profile.getId();

        profilesManager
                .getProfile(id)
                .thenAccept(existingProfile -> {
                    if(existingProfile == null) {
                        payload.sendResponse(new ProfileUpdatePayload(ProfileQueryResult.NOT_FOUND));
                        return;
                    }

                    profilesManager.updateProfile(profile);

                    payload.sendResponse(new ProfileUpdatePayload(ProfileQueryResult.UPDATED_PROFILE));
                });
    }

}
