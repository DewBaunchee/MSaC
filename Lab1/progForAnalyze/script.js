window.onload = function (event) {
    while (choose()) { }
    close();

    function choose() {
        switch (prompt("Which lab are you want to execute?").toLowerCase()) {
            case "2.3":
                lab2_3();
                return true;
            case "2.4":
                lab2_4();
                return true;
            case "3.1":
                lab3_1();
                return true;
            case "exit":
                return false;
            default:
                return true;
        }
    }

    function lab2_3() {
        let n = 0;
        let invalid = true;

        do {
            try {
                n = prompt("Enter matrix length (0 < n < 16)");
                if (n > 0 && n < 16) {
                    invalid = false;
                } else {
                    alert("Value is out of bounds");
                }
            } catch (e) {
                alert("Try again");
            }
        } while (invalid);

        let matrix = new Array(n);
        for (let i = 0; i < n; i++) {
            matrix[i] = new Array(n);
        }

        for (let i = 0; i < n; i++) {
            invalid = true;
            do {
                try {
                    let temp = prompt("Enter matrix line:").splitToInt(" ", n);
                    for (let j = 0; j < n; j++) {
                        matrix[i][j] = temp[j];
                    }
                    invalid = false;
                } catch (e) {
                    alert("INPUT ERROR");
                }
            } while (invalid);
        }

        alert(isIdentity(matrix) ? "Matrix is identity" : "Matrix is not identity");

        function splitToArr(str, count) {
            let line = new Array(count);
            let i = 0;

            while (str.indexOf(" ") > 0 && i < count) {
                line[i] = str.substring(0, str.indexOf(" "));
                line = line.replace(line[i] + " ", "");
                i++;
            }

            line[i] = str;
            return line;
        }

        function isIdentity(matrix) {
            for (let row = 0; row < matrix.length; row++) {
                let col = 0;
                while (col < matrix[row].length) {
                    if (col == row) {
                        if (matrix[row][col] == "1") {
                            col++;
                        } else {
                            return false;
                        }
                    } else {
                        if (matrix[row][col] == "0") {
                            col++;
                        } else {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
    }

    function lab2_4() {
        let a = inputArray();
        let b = inputArray();

        a = sort(a);
        b = sort(b);
        alert(arrToStr(sort(concatArray(a, b))));

        function sort(arr) {
            for (let i = 0; i < arr.length; i++) {
                for (let j = 0; j < arr.length; j++) {
                    if (arr[i] < arr[j]) {
                        let temp = arr[j];
                        arr[j] = arr[i];
                        arr[i] = temp;
                    }
                }
            }
            return arr;
        }

        function arrToStr(arr) {
            let answer = "";
            for (let i = 0; i < arr.length; i++) {
                answer = answer + arr[i] + " ";
            }
            return answer;
        }

        function concatArray(arrA, arrB) {
            let c = new Array(arrA.length + arrB.length);

            for (let i = 0; i < arrA.length; i++) {
                c[i] = arrA[i];
            }
            for (let i = arrA.length; i - arrA.length < arrB.length; i++) {
                c[i] = arrB[i - arrA.length];
            }
            return c;
        }

        function inputArray() {
            let invalid = true;
            let arr;
            let n;
            do {
                try {
                    n = Number.parseInt(prompt("Enter array size"));
                    invalid = false;
                } catch (e) {
                    alert("INPUT ERROR");
                }
            } while (invalid);

            invalid = true;
            do {
                  try {
                arr = splitToInt(prompt("Enter array elements").trim(), n);
                invalid = false;
                 } catch (e) {
                   alert("INPUT ERROR");
                 }
            } while (invalid);

            return arr;
        }

        function splitToInt(str, size) {
            let a = new Array(size);
            let i = 0;
            while (str.indexOf(" ") > 0 && i < size) {
                a[i] = Number.parseInt(str.substring(0,
                    str.indexOf(" ")));
                str = str.replace(a[i] + " ", "");
                i++;
            }
            a[i] = Number.parseInt(str);
            return a;
        }
    }

    function lab3_1() {
        let str1 = inputString();
        let str2 = inputString();
        let k = inputNumber();

        alert(`${definition(str1, str2, k)} - answer`)

        function inputString() {
            return prompt("Enter string:");
        }

        function inputNumber() {
            let invalid = true;
            let k = 0;
            do {
                k = Number.parseInt(prompt("Enter K:"));
                invalid = false;
            } while (invalid);
            return k;
        }

        function definition(s1, s2, krit) {
            let deletedLength = 0;
            alert(s1.indexOf(s2));
            while (krit > 1 && s1.indexOf(s2) > -1) {
                alert(s1 + " " + s2 + " " + krit);
                deletedLength += s2.length+ s1.indexOf(s2);
                s1 = s1.replace(s1.substring(0, s1.indexOf(s2)) + s2, "");
                krit--;
            }
            if (s1.indexOf(s2) < 0) {
                return 0;
            } else {
                return s1.indexOf(s2) + deletedLength + 1;
            }
        }
    }
}
