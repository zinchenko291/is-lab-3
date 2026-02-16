import dayjs from 'dayjs';

const Date = ({ children }: { children: string }) => {
  if (!children) return null;
  const date = dayjs(children.replace('[UTC]', ''))
    .locale('ru')
    .format('DD.MM.YYYY / HH:mm');
  return <span>{date}</span>;
};

export default Date;
