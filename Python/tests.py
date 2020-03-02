from grammar import CFG, print_stuff

def create_cfg(filename):
    with open(filename, 'r') as input_file:
        data = input_file.read()
        cfg = CFG.from_str(data)
        return cfg

def test_create_cfg():
    cfg = create_cfg("./test_cfgs/empty.cfg")
    assert cfg

# def func(x):
#     return x + 1

# def test_answer():
#     assert func(3) == 5