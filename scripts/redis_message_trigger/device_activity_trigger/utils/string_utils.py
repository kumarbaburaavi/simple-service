def to_snake_case(camel_case_str):
    res = [camel_case_str[0].lower()]
    for c in camel_case_str[1:]:
        if c in 'ABCDEFGHIJKLMNOPQRSTUVWXYZ':
            res.append('_')
            res.append(c.lower())
        else:
            res.append(c)

    return ''.join(res)


def to_camel_case(snake_case_str):
    split_str = snake_case_str.split('_')
    return split_str[0] + ''.join(ele.title() for ele in split_str[1:])
