let x = prompt("LABALABA");
        let y;
        if(x < 0) {
            y = 1;
        } else {
            if(x == 0) {
                y = 2;
            } else {
                if(x <= 0.5) {
                    y = 3;
                } else {
                    if(x < 1) {
                        y = 4;
                    } else {
                        y = 5; // default
                    }
                }
            }
        }