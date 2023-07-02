function splitToArr(str: string, count: number): string[] {
    let line: string[] = new String[count];
    let i: number = 0;

    while (str.indexOf(" ") > 0 && i < count) {
        line[i] = str.substring(0, str.indexOf(" "));
        str = str.replace(line[i] + " ", "");
        i++;
    }

    line[i] = str;
    return line;
}