let deletedLength: number = 0;
let str = prompt("s1");
let s1 = str;
str = prompt("s2");
let s2 = str;
let krit = Number.parseInt(prompt("Krit:");
let test = prompt("");
s2 = s2 + test;
test = "";
alert(s1.indexOf(s2));
while (krit > 1 && s1.indexOf(s2) > -1) {
    alert(s1 + " " + s2 + " " + krit);
    deletedLength += s2.length + s1.indexOf(s2);
    s1 = s1.replace(s1.substring(0, s1.indexOf(s2)) + s2, "");
    krit--;
}
if (s1.indexOf(s2) < 0) {
    alert(0);
} else {
    let userInfo: [string, number];
    alert(s1.indexOf(s2) + deletedLength + 1);
}