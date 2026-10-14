0 : {
		bind(2400);
		stop();
		changeFace(2);
		fadeinAnimation( 315.0 , 315.0 , 315.0 , 4.5 , 0, 0.2) ;
		ignoreAttack(2400);
		makeParticle("cold.ptc",0.0,0.0,0.0,0,1,0);
		makeShapeModel("/effect/lcharge.oa",0,0.3,0,0,1,0);
	}
350 : {
		changeFace(1);
		startAnimation( 316.0 , 317.0 , 316.0 , 5 , 1) ;
	}
400 : {
		turn(0);
		move(0,2.2);
		makeShapeModel("/effect/cold1.oa",0,0.0,0.8,0,1,0);
		makeShapeModel("/effect/iceshot_clash.oa",0,0.1,0.8,0,0,1);
		attack("spin");
	}
570 : {
		turn(45);
		move(0,2.2);
		makeShapeModel("/effect/cold1.oa",0,0.0,0.8,0,1,0);
		makeShapeModel("/effect/iceshot_clash.oa",0,0.1,0.8,0,0,1);
		attack("spin");
	}
740 : {
		turn(90);
		move(0,2.2);
		makeShapeModel("/effect/cold1.oa",0,0.0,0.8,0,1,0);
		makeShapeModel("/effect/iceshot_clash.oa",0,0.1,0.8,0,0,1);
		attack("spin");
	}
910 : {
		turn(135);
		move(0,2.2);
		makeShapeModel("/effect/cold1.oa",0,0.0,0.8,0,1,0);
		makeShapeModel("/effect/iceshot_clash.oa",0,0.1,0.8,0,0,1);
		attack("spin");
	}
1080 : {
		turn(180);
		move(0,2.2);
		makeShapeModel("/effect/cold1.oa",0,0.0,0.8,0,1,0);
		makeShapeModel("/effect/iceshot_clash.oa",0,0.1,0.8,0,0,1);
		attack("spin");
	}
1250 : {
		turn(225);
		move(0,2.2);
		makeShapeModel("/effect/cold1.oa",0,0.0,0.8,0,1,0);
		makeShapeModel("/effect/iceshot_clash.oa",0,0.1,0.8,0,0,1);
		attack("spin");
	}
1420 : {
		turn(270);
		move(0,2.2);
		makeShapeModel("/effect/cold1.oa",0,0.0,0.8,0,1,0);
		makeShapeModel("/effect/iceshot_clash.oa",0,0.1,0.8,0,0,1);
		attack("spin");
	}
1590 : {
		turn(315);
		move(0,2.2);
		makeShapeModel("/effect/cold1.oa",0,0.0,0.8,0,1,0);
		makeShapeModel("/effect/iceshot_clash.oa",0,0.1,0.8,0,0,1);
		attack("spin");
	}
1760 : {
		stop();
		turn(0);
		makeShapeModel("/effect/tornado.oa",0,0.0,0,0,1,0);
		makeShapeModel("/effect/iceshot_hadou.oa",0.7,0.1,0,1,0,0);
		makeShapeModel("/effect/iceshot_hadou.oa",-0.7,0.1,0,-1,0,0);
		makeShapeModel("/effect/iceshot_hadou.oa",0,0.1,0.7,0,0,1);
		makeShapeModel("/effect/iceshot_hadou.oa",0,0.1,-0.7,0,0,-1);
		attack("spinEnd0");
		attack("spinEnd90");
		attack("spinEnd180");
		attack("spinEnd270");
		play("/sound/7.sr",100,1);
	}
1910 : {
		startAnimation( 317.0 , 318.0 , 318.0 , 12 , 0) ;
	}
2060 : {
		startAnimation( 318.0 , 319.0 , 319.0 , 4 , 0) ;
	}
2460 : {
		recover();
	}
