export function createUserData(uid, email, username, createdAt) {
    return { uid, email, username, createdAt };
}
export function isTutorialCompleted(userId) {
    return localStorage.getItem(
        "smartTutor_tutorial_completed_" + userId
    ) === "true";
}

export function completeTutorial(userId) {
    localStorage.setItem(
        "smartTutor_tutorial_completed_" + userId,
        "true"
    );
}
