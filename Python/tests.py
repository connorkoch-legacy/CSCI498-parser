from grammar import CFG, print_stuff

def cfg_from_file(filename):
    with open(filename, 'r') as input_file:
        data = input_file.read()
        cfg = CFG.from_str(data)
        return cfg

def test_from_str(snapshot):
    data = "S -> lambda $"

    cfg = CFG.from_str(data)
    print(cfg)
    snapshot.assert_match(cfg)

def test_sampleCFG(snapshot):
    cfg = cfg_from_file("./test_cfgs/sampleCFG.txt")
    print("\n./test_cfgs/sampleCFG.txt")
    print(cfg)
    snapshot.assert_match(cfg)

def test_sampleCFG2(snapshot):
    cfg = cfg_from_file("./test_cfgs/sampleCFG2.txt")
    print("\n./test_cfgs/sampleCFG2.txt")
    print(cfg)
    snapshot.assert_match(cfg)

def test_sample(snapshot):
    cfg = cfg_from_file("./test_cfgs/sample.txt")
    print("\n./test_cfgs/sample.txt")
    print(cfg)
    snapshot.assert_match(cfg)

def test_sample2(snapshot):
    cfg = cfg_from_file("./test_cfgs/sample2.txt")
    print("\n./test_cfgs/sample2.txt")
    print(cfg)
    snapshot.assert_match(cfg)

def test_predict_set_example(snapshot):
    cfg = cfg_from_file("./test_cfgs/predict_set_example.txt")
    print("\n./test_cfgs/predict_set_example.txt")
    print(cfg)
    snapshot.assert_match(cfg)

def test_regex_grammar(snapshot):
    cfg = cfg_from_file("./test_cfgs/lga-ll1-parsing_grammar.cfg")
    print("\n./test_cfgs/lga-ll1-parsing_grammar.txt")
    print(cfg.production_rules)
    print(cfg)
    snapshot.assert_match(cfg)

def test_closure(snapshot):
    cfg = cfg_from_file("./test_cfgs/item_tests/closure-test.cfg")
    # print("\n./test_cfgs/lga-ll1-parsing_grammar.txt")
    print(cfg.production_rules)
    print(cfg)
    assert false
    # snapshot.assert_match(cfg)
