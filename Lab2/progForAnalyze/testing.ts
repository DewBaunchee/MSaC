enum d {S = 6, Y = 3};
        for(let a = 0; a < 10; a++) {
        if(a == 8) continue;
            for(let b = 0; b < 10; b++) {
                for(let c = 0; c < 10; c++) {
                    for(let d = 0; d < 10; d++) {
                        for(let e = 0; e < 10; e++) {
                            for(let f = 0; f < 10; f++) {
                                switch(f) {
                                    case 0: alert("f = 0"); break;
                                    case 2: alert("f = 2"); break;
                                    case 4: alert("f = 4"); break;
                                    case 6:
                                    case 8: if(f == 6) {
                                        alert("f = 6");
                                    } else {
                                        alert("f = 8");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }