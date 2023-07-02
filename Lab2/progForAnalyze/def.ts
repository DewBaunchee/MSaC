function definition(s1: string, s2: string, krit: number, ...daring): number {
    let deletedLength: number = 0;
    alert(s1.indexOf(s2));
    while (krit > 1 && s1.indexOf(s2) > -1) {
        alert(s1 + " " + s2 + " " + krit);
        deletedLength += s2.length + s1.indexOf(s2);
        s1 = s1.replace(s1.substring(0, s1.indexOf(s2)) + s2, "");
        krit--;
    }
    if (s1.indexOf(s2) < 0) {
        return 0;
    } else {
        let userInfo: [string, number];
        return s1.indexOf(s2) + deletedLength + 1;
    }
}